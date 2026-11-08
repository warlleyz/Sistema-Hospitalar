package br.edu.pucminas.hospital.atendimentos;

import br.edu.pucminas.hospital.compartilhado.contratos.*;
import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/** Dados apenas de teste; nunca registrados na aplicacao de producao. */
@TestConfiguration
public class CadastrosTestConfig {
    @Bean ConsultaPacientes pacientesTeste() {
        return id -> id == 1L || id == 2L ? Optional.of(new PacienteResumo(id, "Paciente " + id)) : Optional.empty();
    }
    @Bean ConsultaProfissionais profissionaisTeste() {
        return id -> switch (id.intValue()) {
            case 10 -> Optional.of(new ProfissionalResumo(id, "Clinico", CategoriaProfissional.CLINICO_GERAL, "Clinica"));
            case 11 -> Optional.of(new ProfissionalResumo(id, "Outro clinico", CategoriaProfissional.CLINICO_GERAL, "Clinica"));
            case 20 -> Optional.of(new ProfissionalResumo(id, "Especialista", CategoriaProfissional.ESPECIALISTA, "Cardiologia"));
            case 30 -> Optional.of(new ProfissionalResumo(id, "Cirurgiao", CategoriaProfissional.CIRURGIAO, "Cirurgia"));
            default -> Optional.empty();
        };
    }
}
