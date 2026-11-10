package br.edu.pucminas.hospital.quartos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:quartos-test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "spring.jackson.deserialization.fail-on-unknown-properties=true"})
@AutoConfigureMockMvc
class QuartoApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach void limpar() {
        jdbc.update("DELETE FROM internacao");
        jdbc.update("DELETE FROM quarto");
    }

    private long cadastrar(String json) throws Exception {
        var resultado = mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn();
        return mapper.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test void cadastraListaConsultaEAtualizaQuartos() throws Exception {
        long id = cadastrar("{\"numero\":\" 101a \",\"andar\":1,\"capacidadeMaxima\":3}");
        mvc.perform(get("/quartos/" + id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.numero").value("101A"))
            .andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
            .andExpect(jsonPath("$.ocupacaoAtual").value(0))
            .andExpect(jsonPath("$.vagasDisponiveis").value(3));
        mvc.perform(get("/quartos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(put("/quartos/" + id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"numero\":\"101B\",\"andar\":-1,\"capacidadeMaxima\":2}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.numero").value("101B"))
            .andExpect(jsonPath("$.andar").value(-1)).andExpect(jsonPath("$.vagasDisponiveis").value(2));
        mvc.perform(get("/quartos/" + id)).andExpect(jsonPath("$.capacidadeMaxima").value(2));
    }

    @Test void rejeitaCapacidadeInvalidaNumeroDuplicadoEFormatos() throws Exception {
        cadastrar("{\"numero\":\"A-01\",\"andar\":0,\"capacidadeMaxima\":1}");
        mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"numero\":\" a-01 \",\"andar\":0,\"capacidadeMaxima\":3}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("REGRA_NEGOCIO"));
        mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"numero\":\"B\",\"andar\":0,\"capacidadeMaxima\":0}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"numero\":\"\",\"andar\":0,\"capacidadeMaxima\":2}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"numero\":\"B\",\"andar\":0,\"capacidadeMaxima\":2,\"ocupacaoAtual\":99}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/quartos/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/quartos/invalido")).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM quarto", Long.class)).isEqualTo(1);
    }
}
