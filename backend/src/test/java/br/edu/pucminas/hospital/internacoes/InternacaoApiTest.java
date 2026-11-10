package br.edu.pucminas.hospital.internacoes;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:internacoes-test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "spring.jackson.deserialization.fail-on-unknown-properties=true"})
@AutoConfigureMockMvc
@Import(CadastrosInternacoesTestConfig.class)
class InternacaoApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach void limpar() {
        jdbc.update("DELETE FROM internacao");
        jdbc.update("DELETE FROM quarto");
    }

    private long quarto(int capacidade) throws Exception {
        var resultado = mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"numero\":\"A-01\",\"andar\":1,\"capacidadeMaxima\":" + capacidade + "}"))
            .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    private String entrada(long paciente, long profissional, long quarto) {
        return """
            {"pacienteId":%d,"profissionalId":%d,"quartoId":%d,
             "dataEntrada":"2026-11-20","dataPrevistaAlta":"2026-11-23","observacoes":"Entrada"}
            """.formatted(paciente, profissional, quarto);
    }

    private long internar(String json) throws Exception {
        var resultado = mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn();
        return mapper.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    private void alta(long id, String data) throws Exception {
        mvc.perform(patch("/internacoes/" + id + "/alta").contentType(MediaType.APPLICATION_JSON)
            .content("{\"dataAltaEfetiva\":\"" + data + "\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("ENCERRADA"));
    }

    @Test void ocupaQuartoComVagaERejeitaLotacao() throws Exception {
        long quartoId = quarto(2);
        long id = internar(entrada(1, 10, quartoId));
        mvc.perform(get("/internacoes/" + id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.pacienteId").value(1))
            .andExpect(jsonPath("$.situacao").value("ATIVA"))
            .andExpect(jsonPath("$.dataAltaEfetiva").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(get("/quartos/" + quartoId)).andExpect(status().isOk())
            .andExpect(jsonPath("$.situacao").value("OCUPADO"))
            .andExpect(jsonPath("$.ocupacaoAtual").value(1))
            .andExpect(jsonPath("$.vagasDisponiveis").value(1));
        internar(entrada(2, 11, quartoId));
        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON).content(entrada(3, 10, quartoId)))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("REGRA_NEGOCIO"));
        mvc.perform(get("/quartos/" + quartoId)).andExpect(jsonPath("$.vagasDisponiveis").value(0));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM internacao", Long.class)).isEqualTo(2);
    }

    @Test void validaPacienteProfissionalQuartoEDatas() throws Exception {
        long quartoId = quarto(2);
        for (String json : new String[]{entrada(99, 10, quartoId), entrada(1, 99, quartoId), entrada(1, 10, 99999)}) {
            mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
        }
        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON)
            .content(entrada(1, 10, quartoId).replace("2026-11-23", "2026-11-19")))
            .andExpect(status().isConflict());
        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON)
            .content(entrada(1, 10, quartoId).replace("2026-11-20", "data-invalida")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON)
            .content(entrada(1, 10, quartoId).replace("\"pacienteId\":1", "\"pacienteId\":-1")))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/internacoes/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/internacoes?pacienteId=-1")).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM internacao", Long.class)).isZero();
    }

    @Test void atualizaApenasCamposPermitidosEnquantoAtiva() throws Exception {
        long quartoId = quarto(1);
        long id = internar(entrada(1, 10, quartoId));
        mvc.perform(put("/internacoes/" + id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"profissionalId\":11,\"dataPrevistaAlta\":\"2026-11-25\",\"observacoes\":\"Acompanhamento\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.profissionalId").value(11))
            .andExpect(jsonPath("$.observacoes").value("Acompanhamento"))
            .andExpect(jsonPath("$.dataPrevistaAlta").value("2026-11-25"));
        mvc.perform(put("/internacoes/" + id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"profissionalId\":99}"))
            .andExpect(status().isNotFound());
        mvc.perform(put("/internacoes/" + id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"profissionalId\":11,\"dataPrevistaAlta\":\"2026-11-19\"}"))
            .andExpect(status().isConflict());
        mvc.perform(put("/internacoes/" + id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"profissionalId\":11,\"quartoId\":999}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/internacoes/" + id)).andExpect(jsonPath("$.profissionalId").value(11));
    }

    @Test void previsaoNaoLiberaVagaMasAltaEfetivaLiberaESemRepeticao() throws Exception {
        long quartoId = quarto(1);
        long id = internar(entrada(1, 10, quartoId));
        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON).content(entrada(2, 10, quartoId)))
            .andExpect(status().isConflict());
        mvc.perform(patch("/internacoes/" + id + "/alta").contentType(MediaType.APPLICATION_JSON)
            .content("{\"dataAltaEfetiva\":\"2026-11-19\"}"))
            .andExpect(status().isConflict());
        mvc.perform(get("/quartos/" + quartoId)).andExpect(jsonPath("$.ocupacaoAtual").value(1));
        alta(id, "2026-11-21");
        mvc.perform(get("/quartos/" + quartoId)).andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
            .andExpect(jsonPath("$.ocupacaoAtual").value(0))
            .andExpect(jsonPath("$.vagasDisponiveis").value(1));
        mvc.perform(patch("/internacoes/" + id + "/alta").contentType(MediaType.APPLICATION_JSON)
            .content("{\"dataAltaEfetiva\":\"2026-11-22\"}"))
            .andExpect(status().isConflict());
        mvc.perform(put("/internacoes/" + id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"profissionalId\":10}"))
            .andExpect(status().isConflict());
        internar(entrada(2, 10, quartoId));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM internacao", Long.class)).isEqualTo(2);
    }

    @Test void historicoPreservaAltasESeparePacientes() throws Exception {
        long quartoId = quarto(3);
        long primeira = internar(entrada(1, 10, quartoId));
        alta(primeira, "2026-11-21");
        internar(entrada(2, 11, quartoId));
        internar(entrada(1, 11, quartoId));
        mvc.perform(get("/internacoes/paciente/1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].dataAltaEfetiva").value("2026-11-21"))
            .andExpect(jsonPath("$[0].observacoes").value("Entrada"))
            .andExpect(jsonPath("$[0].pacienteId").value(1))
            .andExpect(jsonPath("$[1].pacienteId").value(1));
        mvc.perform(get("/internacoes?pacienteId=2")).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].pacienteId").value(2));
        mvc.perform(get("/internacoes")).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/internacoes/paciente/99")).andExpect(status().isNotFound());
    }

    @Test void capacidadeNaoPodeCairAbaixoDaOcupacao() throws Exception {
        long quartoId = quarto(2);
        long id = internar(entrada(1, 10, quartoId));
        internar(entrada(2, 11, quartoId));
        String reduzir = "{\"numero\":\"A-01\",\"andar\":1,\"capacidadeMaxima\":1}";
        mvc.perform(put("/quartos/" + quartoId).contentType(MediaType.APPLICATION_JSON).content(reduzir))
            .andExpect(status().isConflict());
        alta(id, "2026-11-21");
        mvc.perform(put("/quartos/" + quartoId).contentType(MediaType.APPLICATION_JSON).content(reduzir))
            .andExpect(status().isOk()).andExpect(jsonPath("$.vagasDisponiveis").value(0));
        mvc.perform(get("/quartos/" + quartoId)).andExpect(jsonPath("$.ocupacaoAtual").value(1));
    }
}
