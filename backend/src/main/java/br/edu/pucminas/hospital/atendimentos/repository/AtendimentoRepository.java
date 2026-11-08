package br.edu.pucminas.hospital.atendimentos.repository;

import br.edu.pucminas.hospital.atendimentos.model.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {
    List<Atendimento> findAllByOrderByInicioPrevistoAscIdAsc();
    List<Atendimento> findByPacienteIdOrderByInicioPrevistoAscIdAsc(Long pacienteId);
}
