package br.edu.pucminas.hospital.pacientes;

import br.edu.pucminas.hospital.compartilhado.contratos.ConsultaPacientes;
import br.edu.pucminas.hospital.compartilhado.contratos.PacienteResumo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pacientes-api")
@AutoConfigureMockMvc
class PacienteApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ConsultaPacientes pacientes;

    @Test
    void cadastraListaConsultaEAtualizaComMesmoId() throws Exception {
        long id = cadastrar("123.456.789-09");
        mvc.perform(get("/pacientes"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[*].cpf", hasItem("12345678909")));
        mvc.perform(get("/pacientes/{id}", id))
            .andExpect(status().isOk()).andExpect(jsonPath("$.dataNascimento").value("1995-02-15"))
            .andExpect(jsonPath("$.telefone").value("(31) 99999-0000"))
            .andExpect(jsonPath("$.endereco").value("Rua Exemplo, 12"))
            .andExpect(jsonPath("$.email").value("ana@example.org"));
        mvc.perform(put("/pacientes/{id}", id).contentType(APPLICATION_JSON)
                .content(pedido("12345678909").replace("Ana Exemplo", "Ana Atualizada")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        assertThat(pacientes.buscarPorId(id)).contains(new PacienteResumo(id, "Ana Atualizada"));
    }

    @Test
    void rejeitaCamposInvalidosNoCadastroENaAtualizacao() throws Exception {
        long id = cadastrar("12345678901");
        String invalido = pedido("123x45678901").replace("1995-02-15", "2999-02-15")
            .replace("ana@example.org", "email-invalido").replace("Ana Exemplo", "")
            .replace("(31) 99999-0000", "telefone");
        for (var requisicao : List.of(post("/pacientes"), put("/pacientes/{id}", id))) {
            mvc.perform(requisicao.contentType(APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"))
                .andExpect(jsonPath("$.campos[*].campo", hasItem("cpf")))
                .andExpect(jsonPath("$.campos[*].campo", hasItem("dataNascimento")))
                .andExpect(jsonPath("$.campos[*].campo", hasItem("email")))
                .andExpect(jsonPath("$.campos[*].campo", hasItem("nome")))
                .andExpect(jsonPath("$.campos[*].campo", hasItem("telefone")));
        }
        assertThat(pacientes.buscarPorId(id)).contains(new PacienteResumo(id, "Ana Exemplo"));
    }

    @Test
    void rejeitaCpfDuplicadoNoCadastroENaAtualizacao() throws Exception {
        cadastrar("123.456.789-02");
        long id = cadastrar("12345678903");
        String duplicado = pedido("12345678902");
        mvc.perform(post("/pacientes").contentType(APPLICATION_JSON).content(duplicado))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONFLITO_DADOS"));
        mvc.perform(put("/pacientes/{id}", id).contentType(APPLICATION_JSON).content(duplicado))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONFLITO_DADOS"));
        mvc.perform(get("/pacientes/{id}", id))
            .andExpect(status().isOk()).andExpect(jsonPath("$.cpf").value("12345678903"));
    }

    @Test
    void distingueIdInvalidoDePacienteInexistente() throws Exception {
        mvc.perform(get("/pacientes/0"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"));
        mvc.perform(get("/pacientes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
        mvc.perform(put("/pacientes/{id}", Long.MAX_VALUE).contentType(APPLICATION_JSON)
                .content(pedido("12345678904")))
            .andExpect(status().isNotFound());
        assertThat(pacientes.buscarPorId(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void cadastrosSimultaneosComOMesmoCpfPersistemUmaUnicaLinha() throws Exception {
        var largada = new CyclicBarrier(2);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var comMascara = executor.submit(() -> cadastrarSimultaneamente("012.345.678-90", largada));
            var semMascara = executor.submit(() -> cadastrarSimultaneamente("01234567890", largada));
            assertThat(List.of(comMascara.get(10, TimeUnit.SECONDS), semMascara.get(10, TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder(201, 409);
        } finally {
            executor.shutdownNow();
        }

        mvc.perform(get("/pacientes")).andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.cpf == '01234567890')]", hasSize(1)));
    }

    private int cadastrarSimultaneamente(String cpf, CyclicBarrier largada) throws Exception {
        largada.await(5, TimeUnit.SECONDS);
        return mvc.perform(post("/pacientes").contentType(APPLICATION_JSON).content(pedido(cpf)))
            .andReturn().getResponse().getStatus();
    }

    private long cadastrar(String cpf) throws Exception {
        var resposta = mvc.perform(post("/pacientes").contentType(APPLICATION_JSON).content(pedido(cpf)))
            .andExpect(status().isCreated()).andReturn().getResponse();
        var corpo = mapper.readTree(resposta.getContentAsString());
        long id = corpo.get("id").asLong();
        assertThat(id).isPositive();
        assertThat(resposta.getHeader("Location")).isEqualTo("/pacientes/" + id);
        assertThat(corpo.get("cpf").asText()).isEqualTo(cpf.replace(".", "").replace("-", ""));
        return id;
    }

    private String pedido(String cpf) {
        return """
            {"nome":"Ana Exemplo","cpf":"%s","dataNascimento":"1995-02-15",
             "telefone":"(31) 99999-0000","endereco":"Rua Exemplo, 12","email":"ana@example.org"}
            """.formatted(cpf);
    }
}
