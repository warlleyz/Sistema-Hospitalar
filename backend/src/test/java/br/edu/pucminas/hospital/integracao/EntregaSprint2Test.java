package br.edu.pucminas.hospital.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Sem simulacoes: verifica as tres frentes com os cadastros e contratos reais. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:entrega-final;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class EntregaSprint2Test {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    long paciente, outroPaciente, medico;

    @BeforeEach void preparar() throws Exception {
        for (String tabela : new String[]{"reserva_agenda", "atendimento", "internacao", "quarto", "profissional_saude", "paciente"})
            jdbc.update("DELETE FROM " + tabela);
        paciente = criar("/pacientes", paciente("12345678909"));
        outroPaciente = criar("/pacientes", paciente("98765432100"));
        medico = criar("/profissionais", Map.of("nome", "Clinico", "registroProfissional", "CRM-AUD",
            "categoria", "CLINICO_GERAL", "especialidade", "Clinica", "telefone", "31999990000", "email", "medico@example.org"));
    }
    private Map<String,Object> paciente(String cpf) {
        return Map.of("nome", "Paciente", "cpf", cpf, "dataNascimento", "1990-01-01",
            "telefone", "31999990000", "endereco", "Rua A", "email", "paciente@example.org");
    }
    private long criar(String caminho, Object corpo) throws Exception {
        var resposta = mvc.perform(post(caminho).contentType(APPLICATION_JSON).content(mapper.writeValueAsString(corpo)))
            .andExpect(status().isCreated()).andReturn().getResponse();
        return mapper.readTree(resposta.getContentAsString()).get("id").asLong();
    }
    private Map<String,Object> consulta(String horario) {
        return Map.of("tipo", "ROTINA", "pacienteId", paciente, "profissionalId", medico,
            "inicioPrevisto", horario, "motivo", "Avaliacao");
    }
    @Test void historicoReuneAtendimentoConcluidoEInternacaoEncerradaSemMisturarPacientes() throws Exception {
        long c = criar("/consultas", consulta("2026-11-20T10:00:00"));
        mvc.perform(patch("/consultas/{id}/situacao", c).contentType(APPLICATION_JSON)
            .content("{\"situacao\":\"EM_ATENDIMENTO\",\"momento\":\"2026-11-20T10:00:00\"}"))
            .andExpect(status().isOk());
        mvc.perform(patch("/consultas/{id}/registro", c).contentType(APPLICATION_JSON)
            .content("{\"observacoes\":\"Orientacoes preservadas\"}")).andExpect(status().isOk());
        mvc.perform(patch("/consultas/{id}/situacao", c).contentType(APPLICATION_JSON)
            .content("{\"situacao\":\"CONCLUIDA\",\"momento\":\"2026-11-20T10:30:00\"}"))
            .andExpect(status().isOk());
        long q = criar("/quartos", Map.of("numero", "H-1", "andar", 1, "capacidadeMaxima", 1, "tipo", "INDIVIDUAL"));
        long i = criar("/internacoes", Map.of("pacienteId", paciente, "profissionalId", medico, "quartoId", q,
            "dataEntrada", "2026-11-20", "observacoes", "Internacao preservada"));
        mvc.perform(patch("/internacoes/{id}/alta", i).contentType(APPLICATION_JSON)
            .content("{\"dataAltaEfetiva\":\"2026-11-21\"}")).andExpect(status().isOk());
        mvc.perform(get("/pacientes/{id}/historico", paciente)).andExpect(status().isOk())
            .andExpect(jsonPath("$.paciente.id").value(paciente))
            .andExpect(jsonPath("$.atendimentos.length()").value(1))
            .andExpect(jsonPath("$.atendimentos[0].situacao").value("CONCLUIDA"))
            .andExpect(jsonPath("$.atendimentos[0].observacoes").value("Orientacoes preservadas"))
            .andExpect(jsonPath("$.internacoes[0].situacao").value("ENCERRADA"))
            .andExpect(jsonPath("$.internacoes[0].observacoes").value("Internacao preservada"));
        mvc.perform(get("/pacientes/{id}/historico", outroPaciente)).andExpect(status().isOk())
            .andExpect(jsonPath("$.atendimentos").isEmpty()).andExpect(jsonPath("$.internacoes").isEmpty());
        mvc.perform(get("/quartos/{id}", q)).andExpect(jsonPath("$.vagasDisponiveis").value(1));
    }
    @Test void historicoRejeitaPacienteInexistenteEIdInvalido() throws Exception {
        mvc.perform(get("/pacientes/999999/historico")).andExpect(status().isNotFound());
        mvc.perform(get("/pacientes/-1/historico")).andExpect(status().isBadRequest());
    }
    @Test void agendaPreservaNoveCasasEInformaIndisponibilidade() throws Exception {
        String inicio = "2026-11-20T15:00:00.123456789";
        long c = criar("/consultas", consulta(inicio));
        mvc.perform(get("/consultas/{id}", c)).andExpect(jsonPath("$.inicioPrevisto").value(inicio));
        mvc.perform(get("/atendimentos/disponibilidade").param("profissionalId", Long.toString(medico)).param("inicio", inicio))
            .andExpect(status().isOk()).andExpect(jsonPath("$.disponivel").value(false));
        mvc.perform(get("/profissionais").param("inicio", inicio))
            .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(post("/consultas").contentType(APPLICATION_JSON).content(mapper.writeValueAsString(consulta(inicio))))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("REGRA_NEGOCIO"));
    }
    @Test void enumsNumericosSaoRejeitadosEmTodosOsNovosContratos() throws Exception {
        String rotina = mapper.writeValueAsString(consulta("2026-11-20T16:00:00"));
        mvc.perform(post("/consultas").contentType(APPLICATION_JSON).content(rotina.replace("\"ROTINA\"", "0")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/emergencias").contentType(APPLICATION_JSON).content(mapper.writeValueAsString(Map.of(
            "pacienteId", paciente, "profissionalId", medico, "entrada", "2026-11-20T17:00:00", "motivo", "Dor", "prioridade", 3))))
            .andExpect(status().isBadRequest());
        long c = criar("/consultas", consulta("2026-11-20T16:00:00"));
        mvc.perform(patch("/consultas/{id}/situacao", c).contentType(APPLICATION_JSON).content("{\"situacao\":5}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/quartos").contentType(APPLICATION_JSON).content("{\"numero\":\"N\",\"andar\":1,\"capacidadeMaxima\":1,\"tipo\":0}"))
            .andExpect(status().isBadRequest());
    }
    @Test void tiposDeQuartosValidamCapacidadeEPreservamEstadoEmErro() throws Exception {
        long q = criar("/quartos", Map.of("numero", "T-1", "andar", 1, "capacidadeMaxima", 2, "tipo", "COLETIVO"));
        mvc.perform(put("/quartos/{id}", q).contentType(APPLICATION_JSON)
            .content("{\"numero\":\"T-1\",\"andar\":1,\"capacidadeMaxima\":2,\"tipo\":\"INDIVIDUAL\"}"))
            .andExpect(status().isConflict());
        mvc.perform(get("/quartos/{id}", q)).andExpect(jsonPath("$.tipo").value("COLETIVO"))
            .andExpect(jsonPath("$.capacidadeMaxima").value(2));
        mvc.perform(put("/quartos/{id}", q).contentType(APPLICATION_JSON)
            .content("{\"numero\":\"T-1\",\"andar\":1,\"capacidadeMaxima\":1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("INDIVIDUAL"));
        mvc.perform(post("/quartos").contentType(APPLICATION_JSON)
            .content("{\"numero\":\"T-2\",\"andar\":1,\"capacidadeMaxima\":1,\"tipo\":\"COLETIVO\"}"))
            .andExpect(status().isConflict());
    }
}
