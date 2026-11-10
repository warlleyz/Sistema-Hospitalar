package br.edu.pucminas.hospital.internacoes.repository;

import br.edu.pucminas.hospital.internacoes.model.Internacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InternacaoRepository extends JpaRepository<Internacao, Long> {
    long countByQuartoIdAndDataAltaEfetivaIsNull(Long quartoId);
    List<Internacao> findAllByOrderByDataEntradaAscIdAsc();
    List<Internacao> findByPacienteIdOrderByDataEntradaAscIdAsc(Long pacienteId);

    /** Consulta escalar evita manter entidade antiga antes do bloqueio do quarto. */
    @Query("select i.quartoId from Internacao i where i.id = :id")
    Optional<Long> obterQuartoId(@Param("id") Long id);
}
