package br.edu.pucminas.hospital.pacientes.dto;

import br.edu.pucminas.hospital.pacientes.model.Paciente;
import java.time.LocalDate;

public record PacienteResposta(Long id, String nome, String cpf, LocalDate dataNascimento,
                              String telefone, String endereco, String email) {
    public static PacienteResposta de(Paciente paciente) {
        return new PacienteResposta(paciente.getId(), paciente.getNome(), paciente.getCpf(),
            paciente.getDataNascimento(), paciente.getTelefone(), paciente.getEndereco(), paciente.getEmail());
    }
}
