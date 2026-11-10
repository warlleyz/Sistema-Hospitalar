package br.edu.pucminas.hospital.quartos.model;

import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;

/** Classificacao por capacidade adotada pelo grupo; nao representa categoria clinica. */
public enum TipoQuarto {
    INDIVIDUAL, COLETIVO;

    public void validarCapacidade(int capacidade) {
        if ((this == INDIVIDUAL && capacidade != 1) || (this == COLETIVO && capacidade < 2))
            throw new RegraNegocioException("Quarto individual exige capacidade 1; coletivo exige capacidade minima 2.");
    }

    public static TipoQuarto porCapacidade(int capacidade) {
        return capacidade == 1 ? INDIVIDUAL : COLETIVO;
    }
}
