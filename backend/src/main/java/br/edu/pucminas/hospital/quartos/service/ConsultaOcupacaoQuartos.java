package br.edu.pucminas.hospital.quartos.service;

/** Contrato entre quartos e internacoes: nao ha contador de ocupacao persistido. */
public interface ConsultaOcupacaoQuartos {
    long ativasNoQuarto(Long quartoId);
}
