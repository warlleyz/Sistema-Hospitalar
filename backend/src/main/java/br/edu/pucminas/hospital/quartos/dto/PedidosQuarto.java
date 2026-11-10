package br.edu.pucminas.hospital.quartos.dto;

import jakarta.validation.constraints.*;
import br.edu.pucminas.hospital.quartos.model.TipoQuarto;

public final class PedidosQuarto {
    private PedidosQuarto() { }

    public record NovoQuarto(
        @NotBlank @Size(max = 32) String numero,
        @NotNull Integer andar,
        @NotNull @Positive Integer capacidadeMaxima,
        TipoQuarto tipo) {
        public NovoQuarto(String numero, Integer andar, Integer capacidadeMaxima) {
            this(numero, andar, capacidadeMaxima, null);
        }
    }

    public record AtualizarQuarto(
        @NotBlank @Size(max = 32) String numero,
        @NotNull Integer andar,
        @NotNull @Positive Integer capacidadeMaxima,
        TipoQuarto tipo) {
        public AtualizarQuarto(String numero, Integer andar, Integer capacidadeMaxima) {
            this(numero, andar, capacidadeMaxima, null);
        }
    }
}
