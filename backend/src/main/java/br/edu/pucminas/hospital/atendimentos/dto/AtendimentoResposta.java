package br.edu.pucminas.hospital.atendimentos.dto;

import br.edu.pucminas.hospital.atendimentos.model.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AtendimentoResposta(
    Long id, String modalidade, Long pacienteId, Long profissionalId,
    LocalDateTime inicioPrevisto, String motivo, SituacaoAtendimento situacao,
    LocalDateTime inicioEfetivo, LocalDateTime fimEfetivo, String observacoes,
    String especialidade, String encaminhamento, Prioridade prioridade,
    String tipoCirurgia, String observacoesPreOperatorias, String observacoesPosOperatorias) {
    public static AtendimentoResposta de(Atendimento a) {
        return new AtendimentoResposta(a.getId(), a.modalidade(), a.getPacienteId(), a.getProfissionalId(),
            a.getInicioPrevisto(), a.getMotivo(), a.getSituacao(), a.getInicioEfetivo(), a.getFimEfetivo(), a.getObservacoes(),
            a instanceof ConsultaEspecializada e ? e.getEspecialidade() : null,
            a instanceof ConsultaRotina r ? r.getEncaminhamento() : null,
            a instanceof Emergencia e ? e.getPrioridade() : null,
            a instanceof Cirurgia c ? c.getTipoCirurgia() : null,
            a instanceof Cirurgia c ? c.getObservacoesPreOperatorias() : null,
            a instanceof Cirurgia c ? c.getObservacoesPosOperatorias() : null);
    }
}
