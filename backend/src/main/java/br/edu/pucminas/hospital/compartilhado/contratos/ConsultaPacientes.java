package br.edu.pucminas.hospital.compartilhado.contratos;

import java.util.Optional;

/** Mateus fornece um bean que implementa este contrato no modulo de pacientes. */
public interface ConsultaPacientes {
    Optional<PacienteResumo> buscarPorId(Long id);
}
