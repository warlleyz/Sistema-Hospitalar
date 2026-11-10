package br.edu.pucminas.hospital.profissionais.dto;

import br.edu.pucminas.hospital.compartilhado.contratos.CategoriaProfissional;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.Locale;

public record ProfissionalPedido(
    @NotBlank @Size(max = 120) String nome,
    @NotBlank @Size(max = 60) String registroProfissional,
    @NotNull @JsonDeserialize(using = CategoriaPorNome.class) CategoriaProfissional categoria,
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

    static final class CategoriaPorNome extends JsonDeserializer<CategoriaProfissional> {
        @Override
        public CategoriaProfissional deserialize(JsonParser json, DeserializationContext contexto) throws IOException {
            if (!json.hasToken(JsonToken.VALUE_STRING)) {
                return contexto.reportInputMismatch(CategoriaProfissional.class, "Informe o nome da categoria profissional.");
            }

            String valor = json.getText().trim();
            try {
                return CategoriaProfissional.valueOf(valor);
            } catch (IllegalArgumentException e) {
                throw contexto.weirdStringException(valor, CategoriaProfissional.class, "Categoria profissional inválida.");
            }
        }
    }
}
