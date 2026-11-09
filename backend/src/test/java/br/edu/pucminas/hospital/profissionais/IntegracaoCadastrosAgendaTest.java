package br.edu.pucminas.hospital.profissionais;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:cadastros-agenda")
@AutoConfigureMockMvc
class IntegracaoCadastrosAgendaTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void atendimentoValidaCadastrosReaisEFiltroConsultaAReservaExistente() throws Exception {
        long paciente = criar("/pacientes", """
            {"nome":"Paciente Exemplo","cpf":"12345678909","dataNascimento":"1995-02-15",
             "telefone":"31999990000","endereco":"Rua Exemplo, 12","email":"paciente@example.org"}
            """);
        long especialista = cadastrarEspecialista("CRM-MG 801");
        long outroEspecialista = cadastrarEspecialista("CRM-MG 802");
        String inicio = "2026-11-20T10:00:00";

        mvc.perform(post("/consultas").contentType(APPLICATION_JSON)
                .content(consulta(Long.MAX_VALUE, especialista, inicio, "Cardiologia")))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
        mvc.perform(post("/consultas").contentType(APPLICATION_JSON)
                .content(consulta(paciente, Long.MAX_VALUE, inicio, "Cardiologia")))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
        mvc.perform(post("/consultas").contentType(APPLICATION_JSON)
                .content(consulta(paciente, especialista, inicio, "Neurologia")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("REGRA_NEGOCIO"));

        long consulta = criar("/consultas", consulta(paciente, especialista, inicio, " cardiologia "));
        mvc.perform(get("/profissionais").param("especialidade", "Cardiologia"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id", containsInAnyOrder((int) especialista, (int) outroEspecialista)));
        mvc.perform(get("/profissionais").param("especialidade", "Cardiologia").param("inicio", inicio))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(outroEspecialista));

        mvc.perform(patch("/consultas/{id}/situacao", consulta).contentType(APPLICATION_JSON)
                .content("""
                    {"situacao":"CANCELADA"}
                    """))
            .andExpect(status().isOk());
        mvc.perform(get("/profissionais").param("especialidade", "Cardiologia").param("inicio", inicio))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id", containsInAnyOrder((int) especialista, (int) outroEspecialista)));
    }

    private long cadastrarEspecialista(String registro) throws Exception {
        return criar("/profissionais", """
            {"nome":"Profissional Exemplo","registroProfissional":"%s","categoria":"ESPECIALISTA",
             "especialidade":"Cardiologia","telefone":"31999990000","email":"equipe@example.org"}
            """.formatted(registro));
    }

    private String consulta(long paciente, long profissional, String inicio, String especialidade) {
        return """
            {"tipo":"ESPECIALIZADA","pacienteId":%d,"profissionalId":%d,
             "inicioPrevisto":"%s","motivo":"Avaliação","especialidade":"%s"}
            """.formatted(paciente, profissional, inicio, especialidade);
    }

    private long criar(String recurso, String corpo) throws Exception {
        var resposta = mvc.perform(post(recurso).contentType(APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated()).andReturn().getResponse();
        return mapper.readTree(resposta.getContentAsString()).get("id").asLong();
    }
}
