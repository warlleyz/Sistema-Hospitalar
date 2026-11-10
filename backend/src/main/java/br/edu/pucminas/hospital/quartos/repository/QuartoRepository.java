package br.edu.pucminas.hospital.quartos.repository;

import br.edu.pucminas.hospital.quartos.model.Quarto;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {
    List<Quarto> findAllByOrderByNumeroAscIdAsc();
    boolean existsByNumero(String numero);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Quarto q where q.id = :id")
    Optional<Quarto> bloquear(@Param("id") Long id);
}
