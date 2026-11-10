package br.edu.pucminas.hospital.internacoes.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class PedidosInternacao {
    private PedidosInternacao() { }

    public record NovaInternacao(
        @NotNull @Positive Long pacienteId,
        @NotNull @Positive Long profissionalId,
        @NotNull @Positive Long quartoId,
        @NotNull LocalDate dataEntrada,
        LocalDate dataPrevistaAlta,
        @Size(max = 4000) String observacoes) { }

    /** Vinculos de paciente/quarto e entrada nao sao editaveis apos o registro. */
    public record AtualizarInternacao(
        @NotNull @Positive Long profissionalId,
        LocalDate dataPrevistaAlta,
        @Size(max = 4000) String observacoes) { }

    public record AltaInternacao(@NotNull LocalDate dataAltaEfetiva) { }
}
