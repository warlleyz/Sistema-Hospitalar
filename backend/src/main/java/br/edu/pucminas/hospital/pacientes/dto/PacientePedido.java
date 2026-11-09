package br.edu.pucminas.hospital.pacientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PacientePedido(
    @NotBlank @Size(max = 120) String nome,
    @NotBlank @Pattern(regexp = "[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}") String cpf,
    @NotNull @PastOrPresent LocalDate dataNascimento,
    @NotBlank @Size(max = 30) @Pattern(regexp = "\\+?[0-9(). -]*[0-9][0-9(). -]*") String telefone,
    @NotBlank @Size(max = 255) String endereco,
    @NotBlank @Email @Size(max = 254) String email
) {
    public PacientePedido {
        if (cpf != null) cpf = cpf.trim();
    }
}
