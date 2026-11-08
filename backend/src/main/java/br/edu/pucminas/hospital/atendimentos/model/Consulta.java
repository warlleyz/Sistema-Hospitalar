package br.edu.pucminas.hospital.atendimentos.model;

import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
public abstract class Consulta extends Atendimento {
    protected Consulta() { }
    protected Consulta(Long paciente, Long profissional, LocalDateTime inicio, String motivo) {
        super(paciente, profissional, inicio, motivo, SituacaoAtendimento.AGENDADA);
    }
    @Override protected Set<SituacaoAtendimento> destinosPermitidos() {
        return switch (getSituacao()) {
            case AGENDADA -> Set.of(SituacaoAtendimento.CONFIRMADA, SituacaoAtendimento.EM_ATENDIMENTO,
                SituacaoAtendimento.CANCELADA, SituacaoAtendimento.NAO_COMPARECEU);
            case CONFIRMADA -> Set.of(SituacaoAtendimento.EM_ATENDIMENTO, SituacaoAtendimento.CANCELADA,
                SituacaoAtendimento.NAO_COMPARECEU);
            case EM_ATENDIMENTO -> Set.of(SituacaoAtendimento.CONCLUIDA);
            default -> Set.of();
        };
    }
}
