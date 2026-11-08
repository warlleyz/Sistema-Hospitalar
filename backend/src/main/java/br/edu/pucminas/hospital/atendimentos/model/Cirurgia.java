package br.edu.pucminas.hospital.atendimentos.model;

import br.edu.pucminas.hospital.compartilhado.contratos.*;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity @DiscriminatorValue("CIRURGIA")
public class Cirurgia extends Atendimento {
    @Column(length = 120) private String tipoCirurgia;
    @Column(length = 4000) private String observacoesPreOperatorias;
    @Column(length = 4000) private String observacoesPosOperatorias;
    protected Cirurgia() { }
    public Cirurgia(Long paciente, Long profissional, LocalDateTime inicio, String descricao, String tipo, String pre) {
        super(paciente, profissional, inicio, descricao, SituacaoAtendimento.AGENDADA);
        tipoCirurgia = texto(tipo, 120, "Tipo de cirurgia");
        if (pre != null) observacoesPreOperatorias = texto(pre, 4000, "Observacoes pre-operatorias");
    }
    @Override public void validarProfissional(ProfissionalResumo profissional) {
        conferirIdentidade(profissional);
        if (profissional.categoria() != CategoriaProfissional.CIRURGIAO)
            throw new RegraNegocioException("Procedimento cirurgico exige cirurgiao.");
    }
    @Override protected Set<SituacaoAtendimento> destinosPermitidos() {
        return switch (getSituacao()) {
            case AGENDADA -> Set.of(SituacaoAtendimento.EM_ATENDIMENTO, SituacaoAtendimento.CANCELADA);
            case EM_ATENDIMENTO -> Set.of(SituacaoAtendimento.CONCLUIDA);
            default -> Set.of();
        };
    }
    public void registrarPosOperatorio(String valor) {
        if (getSituacao() != SituacaoAtendimento.CONCLUIDA)
            throw new RegraNegocioException("Pos-operatorio exige cirurgia concluida.");
        observacoesPosOperatorias = texto(valor, 4000, "Observacoes pos-operatorias");
    }
    public String getTipoCirurgia() { return tipoCirurgia; }
    public String getObservacoesPreOperatorias() { return observacoesPreOperatorias; }
    public String getObservacoesPosOperatorias() { return observacoesPosOperatorias; }
    @Override public String modalidade() { return "CIRURGIA"; }
}
