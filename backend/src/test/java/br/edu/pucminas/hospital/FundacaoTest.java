package br.edu.pucminas.hospital;

import br.edu.pucminas.hospital.compartilhado.contratos.Cadastros;
import br.edu.pucminas.hospital.compartilhado.contratos.ConsultaPacientes;
import br.edu.pucminas.hospital.compartilhado.contratos.ConsultaProfissionais;
import br.edu.pucminas.hospital.compartilhado.erros.DependenciaIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.convention.TestBean;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class FundacaoTest {
    @TestBean(methodName = "cadastrosSemModulos", enforceOverride = true)
    Cadastros cadastros;
    @Autowired MockMvc mvc;

    static Cadastros cadastrosSemModulos() {
        var modulos = new StaticListableBeanFactory();
        return new Cadastros(modulos.getBeanProvider(ConsultaPacientes.class),
            modulos.getBeanProvider(ConsultaProfissionais.class));
    }

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
