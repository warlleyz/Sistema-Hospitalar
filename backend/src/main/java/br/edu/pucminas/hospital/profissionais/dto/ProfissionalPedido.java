package br.edu.pucminas.hospital.profissionais.dto;

import br.edu.pucminas.hospital.compartilhado.contratos.CategoriaProfissional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record ProfissionalPedido(
    @NotBlank @Size(max = 120) String nome,
    @NotBlank @Size(max = 60) String registroProfissional,
    @NotNull CategoriaProfissional categoria,
    @NotBlank @Size(max = 120) String especialidade,
    @Size(max = 120) String areaCirurgica,
    @NotBlank @Size(max = 30) @Pattern(regexp = "\\+?[0-9(). -]*[0-9][0-9(). -]*") String telefone,
    @NotBlank @Email @Size(max = 254) String email
) {
    public ProfissionalPedido {
        if (registroProfissional != null) {
            registroProfissional = registroProfissional.trim().toUpperCase(Locale.ROOT);
        }
    }
}
