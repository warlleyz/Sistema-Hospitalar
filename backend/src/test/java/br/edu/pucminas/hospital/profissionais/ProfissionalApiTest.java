package br.edu.pucminas.hospital.profissionais;

import br.edu.pucminas.hospital.compartilhado.contratos.ConsultaProfissionais;
import br.edu.pucminas.hospital.compartilhado.contratos.ProfissionalResumo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static br.edu.pucminas.hospital.compartilhado.contratos.CategoriaProfissional.ESPECIALISTA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:profissionais-api")
@AutoConfigureMockMvc
class ProfissionalApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ConsultaProfissionais profissionais;

    @Test
    void cadastraCirurgiaoConsultaEAtualizaComMesmoId() throws Exception {
        String cirurgiao = pedido(" crm-mg 100 ", "CIRURGIAO", "Cardiologia", "Cirurgia cardiovascular");
        long id = cadastrar(cirurgiao);
        mvc.perform(get("/profissionais/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.registroProfissional").value("CRM-MG 100"))
            .andExpect(jsonPath("$.categoria").value("CIRURGIAO"))
            .andExpect(jsonPath("$.areaCirurgica").value("Cirurgia cardiovascular"))
            .andExpect(jsonPath("$.telefone").value("(31) 99999-0000"))
            .andExpect(jsonPath("$.email").value("equipe@example.org"));

        mvc.perform(put("/profissionais/{id}", id).contentType(APPLICATION_JSON)
                .content(pedido("CRM-MG 100", "ESPECIALISTA", "Neurologia", null)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.areaCirurgica").isEmpty());
        assertThat(profissionais.buscarPorId(id))
            .contains(new ProfissionalResumo(id, "Profissional Exemplo", ESPECIALISTA, "Neurologia"));
    }

    @Test
    void listaEFiltraEspecialidadeIgnorandoCaixaEEspacosExternos() throws Exception {
        long clinico = cadastrar(pedido("CRM-MG 201", "CLINICO_GERAL", "Clínica geral", null));
        long especialista = cadastrar(pedido("CRM-MG 202", "ESPECIALISTA", " Ortopedia ", null));
        mvc.perform(get("/profissionais"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].registroProfissional", hasItems("CRM-MG 201", "CRM-MG 202")));
        mvc.perform(get("/profissionais").param("especialidade", " oRTOPEDIA "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(especialista));
        mvc.perform(get("/profissionais").param("especialidade", "Clínica geral"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(clinico));
        mvc.perform(get("/profissionais").param("especialidade", "Área inexistente"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejeitaDadosInvalidosNaCriacaoENaAtualizacao() throws Exception {
        long id = cadastrar(pedido("CRM-MG 301", "CLINICO_GERAL", "Clínica geral", null));
        String invalido = pedido(" ", "ESPECIALISTA", " ", null).replace("equipe@example.org", "email-invalido");
        for (var requisicao : java.util.List.of(post("/profissionais"), put("/profissionais/{id}", id))) {
            mvc.perform(requisicao.contentType(APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"))
                .andExpect(jsonPath("$.campos[*].campo", hasItems("registroProfissional", "especialidade", "email")));
        }
        mvc.perform(post("/profissionais").contentType(APPLICATION_JSON)
                .content(pedido("CRM-MG 302", "OUTRA_CATEGORIA", "Cardiologia", null)))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/profissionais/{id}", id).contentType(APPLICATION_JSON)
                .content(pedido("CRM-MG 301", "CIRURGIAO", "Cardiologia", null)))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("REGRA_NEGOCIO"));
        mvc.perform(get("/profissionais/{id}", id))
            .andExpect(status().isOk()).andExpect(jsonPath("$.categoria").value("CLINICO_GERAL"));
    }

    @Test
    void rejeitaRegistroDuplicadoNoCadastroENaAtualizacao() throws Exception {
        cadastrar(pedido("CRM-MG 401", "ESPECIALISTA", "Cardiologia", null));
        long id = cadastrar(pedido("CRM-MG 402", "ESPECIALISTA", "Cardiologia", null));
        String duplicado = pedido(" crm-mg 401 ", "ESPECIALISTA", "Cardiologia", null);
        mvc.perform(post("/profissionais").contentType(APPLICATION_JSON).content(duplicado))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONFLITO_DADOS"));
        mvc.perform(put("/profissionais/{id}", id).contentType(APPLICATION_JSON).content(duplicado))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONFLITO_DADOS"));
        mvc.perform(get("/profissionais/{id}", id))
            .andExpect(status().isOk()).andExpect(jsonPath("$.registroProfissional").value("CRM-MG 402"));
    }

    @Test
    void rejeitaIdEHorarioInvalidosEConsultaInexistente() throws Exception {
        mvc.perform(get("/profissionais/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/profissionais").param("inicio", "amanhã"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("ENTRADA_INVALIDA"));
        mvc.perform(get("/profissionais/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
        mvc.perform(put("/profissionais/{id}", Long.MAX_VALUE).contentType(APPLICATION_JSON)
                .content(pedido("CRM-MG 501", "ESPECIALISTA", "Cardiologia", null)))
            .andExpect(status().isNotFound());
        assertThat(profissionais.buscarPorId(Long.MAX_VALUE)).isEmpty();
    }

    private long cadastrar(String corpo) throws Exception {
        var resposta = mvc.perform(post("/profissionais").contentType(APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated()).andReturn().getResponse();
        long id = mapper.readTree(resposta.getContentAsString()).get("id").asLong();
        assertThat(id).isPositive();
        assertThat(resposta.getHeader("Location")).isEqualTo("/profissionais/" + id);
        return id;
    }

    private String pedido(String registro, String categoria, String especialidade, String areaCirurgica) {
        String area = areaCirurgica == null ? "null" : "\"" + areaCirurgica + "\"";
        return """
            {"nome":"Profissional Exemplo","registroProfissional":"%s","categoria":"%s","especialidade":"%s",
             "areaCirurgica":%s,"telefone":"(31) 99999-0000","email":"equipe@example.org"}
            """.formatted(registro, categoria, especialidade, area);
    }
}
