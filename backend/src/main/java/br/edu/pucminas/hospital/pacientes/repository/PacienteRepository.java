package br.edu.pucminas.hospital.pacientes.repository;

import br.edu.pucminas.hospital.pacientes.model.Paciente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findAllByOrderByIdAsc();
}
