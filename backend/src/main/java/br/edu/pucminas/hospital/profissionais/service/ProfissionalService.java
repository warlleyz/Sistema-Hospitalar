package br.edu.pucminas.hospital.profissionais.service;

import br.edu.pucminas.hospital.compartilhado.contratos.ConsultaDisponibilidade;
import br.edu.pucminas.hospital.compartilhado.contratos.ConsultaProfissionais;
import br.edu.pucminas.hospital.compartilhado.contratos.ProfissionalResumo;
import br.edu.pucminas.hospital.compartilhado.erros.RecursoNaoEncontradoException;
import br.edu.pucminas.hospital.profissionais.dto.ProfissionalPedido;
import br.edu.pucminas.hospital.profissionais.dto.ProfissionalResposta;
import br.edu.pucminas.hospital.profissionais.model.ProfissionalSaude;
import br.edu.pucminas.hospital.profissionais.repository.ProfissionalRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfissionalService implements ConsultaProfissionais {
    private final ProfissionalRepository repository;
    private final ConsultaDisponibilidade disponibilidade;

    public ProfissionalService(ProfissionalRepository repository, ConsultaDisponibilidade disponibilidade) {
        this.repository = repository;
        this.disponibilidade = disponibilidade;
    }

    @Transactional
    public ProfissionalResposta criar(ProfissionalPedido pedido) {
        var profissional = new ProfissionalSaude(pedido.nome(), pedido.registroProfissional(), pedido.categoria(),
            pedido.especialidade(), pedido.areaCirurgica(), pedido.telefone(), pedido.email());
        return ProfissionalResposta.de(repository.save(profissional));
    }

    public List<ProfissionalResposta> listar(String especialidade, LocalDateTime inicio) {
        var profissionais = especialidade == null || especialidade.isBlank()
            ? repository.findAllByOrderByIdAsc()
            : repository.findByEspecialidadeIgnoreCaseOrderByIdAsc(especialidade.trim());
        return profissionais.stream()
            .filter(profissional -> inicio == null || disponibilidade.disponivel(profissional.getId(), inicio))
            .map(ProfissionalResposta::de).toList();
    }

    public ProfissionalResposta consultar(Long id) {
        return ProfissionalResposta.de(obter(id));
    }

    @Transactional
    public ProfissionalResposta atualizar(Long id, ProfissionalPedido pedido) {
        var profissional = obter(id);
        profissional.atualizar(pedido.nome(), pedido.registroProfissional(), pedido.categoria(),
            pedido.especialidade(), pedido.areaCirurgica(), pedido.telefone(), pedido.email());
        return ProfissionalResposta.de(profissional);
    }

    @Override
    public Optional<ProfissionalResumo> buscarPorId(Long id) {
        return repository.findById(id).map(profissional -> new ProfissionalResumo(profissional.getId(),
            profissional.getNome(), profissional.getCategoria(), profissional.getEspecialidade()));
    }

    private ProfissionalSaude obter(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));
    }
}
