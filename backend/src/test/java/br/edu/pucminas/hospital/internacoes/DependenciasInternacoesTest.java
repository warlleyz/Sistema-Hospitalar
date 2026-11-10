package br.edu.pucminas.hospital.internacoes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import br.edu.pucminas.hospital.compartilhado.contratos.*;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.test.context.bean.override.convention.TestBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Sem cadastros integrados, nao fabrica pacientes e profissionais em producao. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:dependencias-internacoes;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class DependenciasInternacoesTest {
    @Autowired MockMvc mvc;
    @TestBean(methodName = "cadastrosSemModulos", enforceOverride = true)
    Cadastros cadastros;

    static Cadastros cadastrosSemModulos() {
        var modulos = new StaticListableBeanFactory();
        return new Cadastros(modulos.getBeanProvider(ConsultaPacientes.class),
            modulos.getBeanProvider(ConsultaProfissionais.class));
    }

    @Test void semModuloPacienteRetorna503() throws Exception {
        var quarto = mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"numero\":\"DEPENDENCIA-1\",\"andar\":1,\"capacidadeMaxima\":1}"))
            .andExpect(status().isCreated()).andReturn();
        long id = new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(quarto.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"pacienteId":1,"profissionalId":10,"quartoId":%d,"dataEntrada":"2026-11-20"}
                """.formatted(id)))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.codigo").value("MODULO_INDISPONIVEL"));
        mvc.perform(get("/internacoes/paciente/1")).andExpect(status().isServiceUnavailable());
        mvc.perform(get("/quartos/" + id)).andExpect(jsonPath("$.ocupacaoAtual").value(0));
    }
}
