package br.edu.pucminas.hospital;

import br.edu.pucminas.hospital.compartilhado.contratos.Cadastros;
import br.edu.pucminas.hospital.compartilhado.erros.DependenciaIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class FundacaoTest {
    @Autowired Cadastros cadastros;
    @Autowired MockMvc mvc;
    @Test void iniciaSemFingirQueCadastrosExistem() {
        assertThatThrownBy(() -> cadastros.paciente(1L)).isInstanceOf(DependenciaIndisponivelException.class);
        assertThatThrownBy(() -> cadastros.profissional(1L)).isInstanceOf(DependenciaIndisponivelException.class);
    }
    @Test void apiExplicaDependenciaAusenteSemCriarCadastrosFalsos() throws Exception {
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content("""
            {"tipo":"ROTINA","pacienteId":1,"profissionalId":10,
             "inicioPrevisto":"2026-11-20T10:00:00","motivo":"Avaliacao"}
            """))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.codigo").value("MODULO_INDISPONIVEL"));
    }
}
