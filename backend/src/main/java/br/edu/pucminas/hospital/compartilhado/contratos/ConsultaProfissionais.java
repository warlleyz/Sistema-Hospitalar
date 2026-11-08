package br.edu.pucminas.hospital.compartilhado.contratos;

import java.util.Optional;

/** Mateus fornece um bean que implementa este contrato no modulo de profissionais. */
public interface ConsultaProfissionais {
    Optional<ProfissionalResumo> buscarPorId(Long id);
}
