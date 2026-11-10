package br.edu.pucminas.hospital.quartos.dto;

import br.edu.pucminas.hospital.quartos.model.*;

public record QuartoResposta(Long id, String numero, Integer andar, Integer capacidadeMaxima,
    SituacaoQuarto situacao, long ocupacaoAtual, long vagasDisponiveis, TipoQuarto tipo) {

    public static QuartoResposta de(Quarto quarto, long ocupacao) {
        return new QuartoResposta(quarto.getId(), quarto.getNumero(), quarto.getAndar(),
            quarto.getCapacidadeMaxima(), ocupacao == 0 ? SituacaoQuarto.DISPONIVEL : SituacaoQuarto.OCUPADO,
            ocupacao, quarto.getCapacidadeMaxima() - ocupacao, quarto.getTipo());
    }
}
