package br.edu.pucminas.hospital.profissionais.repository;

import br.edu.pucminas.hospital.profissionais.model.ProfissionalSaude;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalRepository extends JpaRepository<ProfissionalSaude, Long> {
    List<ProfissionalSaude> findAllByOrderByIdAsc();

    List<ProfissionalSaude> findByEspecialidadeIgnoreCaseOrderByIdAsc(String especialidade);
}
