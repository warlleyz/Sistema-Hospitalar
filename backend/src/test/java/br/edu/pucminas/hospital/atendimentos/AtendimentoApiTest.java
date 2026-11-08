package br.edu.pucminas.hospital.atendimentos;

import br.edu.pucminas.hospital.atendimentos.dto.Pedidos.*;
import br.edu.pucminas.hospital.atendimentos.model.*;
import br.edu.pucminas.hospital.atendimentos.service.AtendimentoService;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:api-test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "spring.jackson.deserialization.fail-on-unknown-properties=true"})
@AutoConfigureMockMvc
@Import(CadastrosTestConfig.class)
class AtendimentoApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired AtendimentoService service;
    private final LocalDateTime inicio = LocalDateTime.of(2026, 11, 20, 10, 0);
    @BeforeEach void limpar() {
        jdbc.update("DELETE FROM reserva_agenda");
        jdbc.update("DELETE FROM atendimento");
    }
    private String rotina(long paciente, long profissional) {
        return "{\"tipo\":\"ROTINA\",\"pacienteId\":" + paciente + ",\"profissionalId\":" + profissional
            + ",\"inicioPrevisto\":\"2026-11-20T10:00:00\",\"motivo\":\"Avaliacao\"}";
    }
    private long criar(String recurso, String json) throws Exception {
        var result = mvc.perform(post("/" + recurso).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
    private void transicao(String recurso, long id, String situacao, String momento) throws Exception {
        var json = "{\"situacao\":\"" + situacao + "\",\"momento\":" + (momento == null ? "null" : "\"" + momento + "\"") + "}";
        mvc.perform(patch("/" + recurso + "/" + id + "/situacao").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk());
    }
    @Test void consultaCompletaRegistraEncaminhamentoEPreservaHistorico() throws Exception {
        long id = criar("consultas", rotina(1, 10));
        mvc.perform(get("/consultas/" + id)).andExpect(jsonPath("$.situacao").value("AGENDADA"));
        transicao("consultas", id, "CONFIRMADA", null);
        transicao("consultas", id, "EM_ATENDIMENTO", "2026-11-20T10:03:00");
        mvc.perform(patch("/consultas/" + id + "/registro").contentType(MediaType.APPLICATION_JSON)
            .content("{\"observacoes\":\"Acompanhamento\",\"encaminhamento\":\"Cardiologia\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.encaminhamento").value("Cardiologia"));
        transicao("consultas", id, "CONCLUIDA", "2026-11-20T10:30:00");
        mvc.perform(get("/consultas?pacienteId=1&situacao=CONCLUIDA")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/consultas?pacienteId=2")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(patch("/consultas/" + id + "/situacao").contentType(MediaType.APPLICATION_JSON).content("{\"situacao\":\"AGENDADA\"}"))
            .andExpect(status().isConflict());
    }
    @Test void rejeitaVinculosInexistentes() throws Exception {
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(rotina(99, 10)))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(rotina(1, 99)))
            .andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM atendimento", Long.class)).isZero();
    }
    @Test void conflitosEntreModalidadesECancelamentoAtomico() throws Exception {
        long id = criar("consultas", rotina(1, 10));
        String emergencia = "{\"pacienteId\":2,\"profissionalId\":10,\"entrada\":\"2026-11-20T10:00:00\",\"motivo\":\"Dor\",\"prioridade\":\"ALTA\"}";
        mvc.perform(post("/emergencias").contentType(MediaType.APPLICATION_JSON).content(emergencia)).andExpect(status().isConflict());
        transicao("consultas", id, "CANCELADA", null);
        criar("emergencias", emergencia);
        mvc.perform(patch("/consultas/" + id + "/situacao").contentType(MediaType.APPLICATION_JSON)
            .content("{\"situacao\":\"CONCLUIDA\",\"momento\":\"2026-11-20T11:00:00\"}"))
            .andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM reserva_agenda", Long.class)).isEqualTo(1);
    }
    @Test void profissionaisDiferentesPodemUsarMesmoHorario() throws Exception {
        criar("consultas", rotina(1, 10));
        criar("consultas", rotina(2, 11));
        mvc.perform(get("/atendimentos/disponibilidade?profissionalId=10&inicio=2026-11-20T10:00:00"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.disponivel").value(false));
    }
    @Test void especializadaExigeCompatibilidade() throws Exception {
        String json = "{\"tipo\":\"ESPECIALIZADA\",\"pacienteId\":1,\"profissionalId\":20,\"inicioPrevisto\":\"2026-11-20T10:00:00\",\"motivo\":\"Revisao\",\"especialidade\":\"Cardiologia\"}";
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(json.replace("Cardiologia", "Neurologia")))
            .andExpect(status().isConflict());
        criar("consultas", json);
    }
    @Test void emergenciaFiltraFilaEConclui() throws Exception {
        long id = criar("emergencias", "{\"pacienteId\":1,\"profissionalId\":10,\"entrada\":\"2026-11-20T10:00:00\",\"motivo\":\"Dor\",\"prioridade\":\"CRITICA\"}");
        mvc.perform(get("/emergencias?situacao=AGUARDANDO"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].prioridade").value("CRITICA"));
        transicao("emergencias", id, "EM_ATENDIMENTO", "2026-11-20T10:01:00");
        transicao("emergencias", id, "CONCLUIDA", "2026-11-20T10:15:00");
        mvc.perform(get("/emergencias?situacao=AGUARDANDO")).andExpect(jsonPath("$.length()").value(0));
    }
    @Test void cirurgiaExigeInicioECirurgiaoERegistraPos() throws Exception {
        String json = "{\"pacienteId\":1,\"profissionalId\":30,\"inicioPrevisto\":\"2026-11-20T10:00:00\",\"descricao\":\"Procedimento\",\"tipoCirurgia\":\"Geral\",\"observacoesPreOperatorias\":\"Pre\"}";
        mvc.perform(post("/cirurgias").contentType(MediaType.APPLICATION_JSON).content(json.replace(":30", ":10")))
            .andExpect(status().isConflict());
        long id = criar("cirurgias", json);
        mvc.perform(patch("/cirurgias/" + id + "/situacao").contentType(MediaType.APPLICATION_JSON)
            .content("{\"situacao\":\"CONCLUIDA\",\"momento\":\"2026-11-20T11:00:00\"}"))
            .andExpect(status().isConflict());
        transicao("cirurgias", id, "EM_ATENDIMENTO", "2026-11-20T10:05:00");
        transicao("cirurgias", id, "CONCLUIDA", "2026-11-20T11:00:00");
        mvc.perform(patch("/cirurgias/" + id + "/registro").contentType(MediaType.APPLICATION_JSON)
            .content("{\"observacoes\":\"Concluida\",\"observacoesPosOperatorias\":\"Estavel\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.observacoesPosOperatorias").value("Estavel"));
        mvc.perform(get("/consultas/" + id)).andExpect(status().isNotFound());
    }
    @Test void rollbackNaoSalvaRegistroClinicoParcial() throws Exception {
        long id = criar("consultas", rotina(1, 10));
        transicao("consultas", id, "EM_ATENDIMENTO", "2026-11-20T10:00:00");
        mvc.perform(patch("/consultas/" + id + "/registro").contentType(MediaType.APPLICATION_JSON)
            .content("{\"observacoes\":\"Nao deve persistir\",\"observacoesPosOperatorias\":\"Incompativel\"}"))
            .andExpect(status().isConflict());
        mvc.perform(get("/consultas/" + id)).andExpect(jsonPath("$.observacoes").doesNotExist());
    }
    @Test void rejeitaEntradasMalformadasEValoresLivres() throws Exception {
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos").isArray());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(rotina(1, 10).replace("ROTINA", "QUALQUER")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(rotina(1, 10).replace("Avaliacao", " ")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(rotina(1, 10).replace("\"motivo\"", "\"campoDesconhecido\"")))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/consultas/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/consultas?pacienteId=-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/atendimentos/disponibilidade?profissionalId=10")).andExpect(status().isBadRequest());
    }
    @Test void duasRequisicoesConcorrentesReservamApenasUmaVagaNaAgenda() throws Exception {
        var pronto = new CountDownLatch(2);
        var largada = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        Callable<Boolean> tentar = () -> {
            pronto.countDown();
            if (!largada.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Sem largada");
            try {
                service.criarConsulta(new NovaConsulta(TipoConsulta.ROTINA, 1L, 10L, inicio, "Concorrente", null));
                return true;
            } catch (RegraNegocioException conflito) { return false; }
        };
        try {
            var a = pool.submit(tentar); var b = pool.submit(tentar);
            assertThat(pronto.await(5, TimeUnit.SECONDS)).isTrue();
            largada.countDown();
            assertThat(java.util.List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder(true, false);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM atendimento", Long.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM reserva_agenda", Long.class)).isEqualTo(1);
        } finally { largada.countDown(); pool.shutdownNow(); }
    }
}
