package br.edu.pucminas.hospital.internacoes.dto;

import br.edu.pucminas.hospital.internacoes.model.*;
import java.time.LocalDate;

public record InternacaoResposta(Long id, Long pacienteId, Long profissionalId, Long quartoId,
    LocalDate dataEntrada, LocalDate dataPrevistaAlta, LocalDate dataAltaEfetiva,
    String observacoes, SituacaoInternacao situacao) {

    public static InternacaoResposta de(Internacao internacao) {
        return new InternacaoResposta(internacao.getId(), internacao.getPacienteId(), internacao.getProfissionalId(),
            internacao.getQuartoId(), internacao.getDataEntrada(), internacao.getDataPrevistaAlta(),
            internacao.getDataAltaEfetiva(), internacao.getObservacoes(),
            internacao.ativa() ? SituacaoInternacao.ATIVA : SituacaoInternacao.ENCERRADA);
    }
}
