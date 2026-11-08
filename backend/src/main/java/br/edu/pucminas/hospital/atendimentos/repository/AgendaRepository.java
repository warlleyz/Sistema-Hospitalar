package br.edu.pucminas.hospital.atendimentos.repository;

import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** O bloqueio no banco serializa escritas da agenda inclusive entre instancias. */
@Repository
public class AgendaRepository {
    private final JdbcTemplate jdbc;
    public AgendaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public void bloquear() {
        jdbc.queryForObject("SELECT id FROM controle_agenda WHERE id = 1 FOR UPDATE", Long.class);
    }
    public boolean ocupado(Long profissional, LocalDateTime inicio) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM reserva_agenda WHERE profissional_id = ? AND inicio_previsto = ?",
            Long.class, profissional, inicio) > 0;
    }
    public void reservar(Long atendimento, Long profissional, LocalDateTime inicio) {
        jdbc.update("INSERT INTO reserva_agenda(atendimento_id, profissional_id, inicio_previsto) VALUES (?, ?, ?)",
            atendimento, profissional, inicio);
    }
    public void liberar(Long atendimento) {
        jdbc.update("DELETE FROM reserva_agenda WHERE atendimento_id = ?", atendimento);
    }
}
