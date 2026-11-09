package br.edu.pucminas.hospital.pacientes;

import br.edu.pucminas.hospital.HospitalApplication;
import br.edu.pucminas.hospital.compartilhado.contratos.Cadastros;
import br.edu.pucminas.hospital.compartilhado.contratos.PacienteResumo;
import br.edu.pucminas.hospital.compartilhado.contratos.ProfissionalResumo;
import br.edu.pucminas.hospital.pacientes.dto.PacientePedido;
import br.edu.pucminas.hospital.pacientes.service.PacienteService;
import br.edu.pucminas.hospital.profissionais.dto.ProfissionalPedido;
import br.edu.pucminas.hospital.profissionais.service.ProfissionalService;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static br.edu.pucminas.hospital.compartilhado.contratos.CategoriaProfissional.CIRURGIAO;
import static org.assertj.core.api.Assertions.assertThat;

class PersistenciaCadastrosTest {
    @TempDir Path pasta;

    @Test
    void preservaAmbosOsCadastrosESeusResumosAoReabrirBanco() {
        String url = "jdbc:h2:file:" + pasta.resolve("cadastros").toAbsolutePath() + ";DB_CLOSE_ON_EXIT=FALSE";
        long pacienteId;
        long profissionalId;
        try (var contexto = iniciar(url)) {
            var pacientes = contexto.getBean(PacienteService.class);
            pacienteId = pacientes.criar(new PacientePedido("Ana Exemplo", "123.456.789-09",
                LocalDate.of(1995, 2, 15), "31999990000", "Rua Exemplo, 12", "ana@example.org")).id();
            pacientes.atualizar(pacienteId, new PacientePedido("Ana Atualizada", "12345678909",
                LocalDate.of(1995, 2, 15), "31999990000", "Rua Exemplo, 30", "ana@example.org"));
            profissionalId = contexto.getBean(ProfissionalService.class).criar(new ProfissionalPedido(
                "Profissional Exemplo", " crm-mg 900 ", CIRURGIAO, "Cardiologia", "Cirurgia cardiovascular",
                "31999990000", "equipe@example.org")).id();
        }
        try (var contexto = iniciar(url)) {
            var cadastros = contexto.getBean(Cadastros.class);
            assertThat(cadastros.paciente(pacienteId)).isEqualTo(new PacienteResumo(pacienteId, "Ana Atualizada"));
            assertThat(cadastros.profissional(profissionalId))
                .isEqualTo(new ProfissionalResumo(profissionalId, "Profissional Exemplo", CIRURGIAO, "Cardiologia"));
            var paciente = contexto.getBean(PacienteService.class).consultar(pacienteId);
            assertThat(paciente.cpf()).isEqualTo("12345678909");
            assertThat(paciente.endereco()).isEqualTo("Rua Exemplo, 30");
            var profissional = contexto.getBean(ProfissionalService.class).consultar(profissionalId);
            assertThat(profissional.registroProfissional()).isEqualTo("CRM-MG 900");
            assertThat(profissional.areaCirurgica()).isEqualTo("Cirurgia cardiovascular");
        }
    }

    private ConfigurableApplicationContext iniciar(String url) {
        return new SpringApplicationBuilder(HospitalApplication.class)
            .web(WebApplicationType.NONE).run("--spring.datasource.url=" + url,
                "--spring.main.banner-mode=off", "--logging.level.root=WARN");
    }
}
