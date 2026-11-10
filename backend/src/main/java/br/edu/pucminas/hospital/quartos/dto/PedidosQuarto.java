package br.edu.pucminas.hospital.quartos.dto;

import jakarta.validation.constraints.*;

public final class PedidosQuarto {
    private PedidosQuarto() { }

    public record NovoQuarto(
        @NotBlank @Size(max = 32) String numero,
        @NotNull Integer andar,
        @NotNull @Positive Integer capacidadeMaxima) { }

    public record AtualizarQuarto(
        @NotBlank @Size(max = 32) String numero,
        @NotNull Integer andar,
        @NotNull @Positive Integer capacidadeMaxima) { }
}
