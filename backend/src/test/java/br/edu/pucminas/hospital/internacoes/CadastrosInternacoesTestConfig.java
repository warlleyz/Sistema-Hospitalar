package br.edu.pucminas.hospital.internacoes;

import br.edu.pucminas.hospital.compartilhado.contratos.*;
import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Implementacoes dos contratos de cadastro exclusivas dos testes. */
@TestConfiguration
public class CadastrosInternacoesTestConfig {
    @Bean @Primary ConsultaPacientes consultaPacientesInternacoesTeste() {
        return id -> id == 1L || id == 2L || id == 3L
            ? Optional.of(new PacienteResumo(id, "Paciente " + id)) : Optional.empty();
    }

    @Bean @Primary ConsultaProfissionais consultaProfissionaisInternacoesTeste() {
        return id -> id == 10L || id == 11L
            ? Optional.of(new ProfissionalResumo(id, "Profissional " + id,
                CategoriaProfissional.CLINICO_GERAL, "Clinica")) : Optional.empty();
    }
}
