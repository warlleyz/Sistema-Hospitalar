package br.edu.pucminas.hospital.atendimentos.model;

import br.edu.pucminas.hospital.compartilhado.contratos.ProfissionalResumo;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity @DiscriminatorValue("EMERGENCIA")
public class Emergencia extends Atendimento {
    @Enumerated(EnumType.STRING) @Column(length = 20) private Prioridade prioridade;
    protected Emergencia() { }
    public Emergencia(Long paciente, Long profissional, LocalDateTime entrada, String motivo, Prioridade prioridade) {
        super(paciente, profissional, entrada, motivo, SituacaoAtendimento.AGUARDANDO);
        if (prioridade == null) throw new RegraNegocioException("Prioridade obrigatoria.");
        this.prioridade = prioridade;
    }
    @Override public void validarProfissional(ProfissionalResumo profissional) { conferirIdentidade(profissional); }
    @Override protected Set<SituacaoAtendimento> destinosPermitidos() {
        return switch (getSituacao()) {
            case AGUARDANDO -> Set.of(SituacaoAtendimento.EM_ATENDIMENTO, SituacaoAtendimento.CANCELADA);
            case EM_ATENDIMENTO -> Set.of(SituacaoAtendimento.CONCLUIDA);
            default -> Set.of();
        };
    }
    @Override public void alterarSituacao(SituacaoAtendimento destino, LocalDateTime momento) {
        if (destino == SituacaoAtendimento.EM_ATENDIMENTO && momento != null && momento.isBefore(getInicioPrevisto()))
            throw new RegraNegocioException("Inicio da emergencia nao pode anteceder sua entrada.");
        super.alterarSituacao(destino, momento);
    }
    public Prioridade getPrioridade() { return prioridade; }
    @Override public String modalidade() { return "EMERGENCIA"; }
}
