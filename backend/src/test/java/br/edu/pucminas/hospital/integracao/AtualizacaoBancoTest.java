package br.edu.pucminas.hospital.integracao;

import java.nio.file.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;

class AtualizacaoBancoTest {
    @Test void atualizaBancoAnteriorSemApagarQuartosAtendimentosOuReservas() throws Exception {
        var pasta = Path.of("target", "upgrade-" + UUID.randomUUID()).toAbsolutePath();
        var antigas = Files.createDirectories(pasta.resolve("migracoes-antigas"));
        for (String arquivo : new String[]{"V100__atendimentos.sql", "V101__agenda_compartilhada.sql",
            "V200__pacientes.sql", "V201__profissionais.sql", "V300__quartos.sql", "V301__internacoes.sql"}) {
            try (var origem = getClass().getResourceAsStream("/db/migration/" + arquivo)) {
                if (origem == null) throw new IllegalStateException(arquivo);
                Files.copy(origem, antigas.resolve(arquivo));
            }
        }
        String url = "jdbc:h2:file:" + pasta.resolve("hospital").toString().replace('\\', '/') + ";DB_CLOSE_ON_EXIT=FALSE";
        Flyway.configure().dataSource(url, "sa", "").locations("filesystem:" + antigas).load().migrate();
        var jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
        jdbc.update("INSERT INTO quarto(numero, andar, capacidade_maxima) VALUES ('A', 1, 1), ('B', 1, 3)");
        var horario = LocalDateTime.of(2026, 11, 20, 10, 0, 0, 123456000);
        jdbc.update("INSERT INTO atendimento(id, modalidade, paciente_id, profissional_id, inicio_previsto, motivo, situacao) VALUES (1, 'ROTINA', 1, 1, ?, 'Preservado', 'AGENDADA')", horario);
        jdbc.update("INSERT INTO reserva_agenda(atendimento_id, profissional_id, inicio_previsto) VALUES (1, 1, ?)", horario);
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").outOfOrder(true).load().migrate();
        assertThat(jdbc.queryForList("SELECT tipo FROM quarto ORDER BY numero", String.class))
            .containsExactly("INDIVIDUAL", "COLETIVO");
        assertThat(jdbc.queryForObject("SELECT motivo FROM atendimento WHERE id = 1", String.class)).isEqualTo("Preservado");
        assertThat(jdbc.queryForObject("SELECT inicio_previsto FROM reserva_agenda WHERE atendimento_id = 1", Timestamp.class)
            .toLocalDateTime()).isEqualTo(horario);
        var preciso = horario.withNano(123456789);
        jdbc.update("UPDATE atendimento SET inicio_previsto = ? WHERE id = 1", preciso);
        jdbc.update("UPDATE reserva_agenda SET inicio_previsto = ? WHERE atendimento_id = 1", preciso);
        assertThat(jdbc.queryForObject("SELECT inicio_previsto FROM reserva_agenda WHERE atendimento_id = 1", Timestamp.class)
            .toLocalDateTime()).isEqualTo(preciso);
        assertThatThrownBy(() -> jdbc.update("UPDATE quarto SET capacidade_maxima = 2 WHERE numero = 'A'"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
