package br.edu.pucminas.hospital.compartilhado.contratos;

import java.time.LocalDateTime;

/** A frente de profissionais pode consultar a agenda sem duplicar suas regras. */
public interface ConsultaDisponibilidade {
    boolean disponivel(Long profissionalId, LocalDateTime inicio);
}
