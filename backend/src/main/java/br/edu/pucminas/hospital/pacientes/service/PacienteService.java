package br.edu.pucminas.hospital.pacientes.service;

import br.edu.pucminas.hospital.compartilhado.contratos.ConsultaPacientes;
import br.edu.pucminas.hospital.compartilhado.contratos.PacienteResumo;
import br.edu.pucminas.hospital.compartilhado.erros.RecursoNaoEncontradoException;
import br.edu.pucminas.hospital.pacientes.dto.PacientePedido;
import br.edu.pucminas.hospital.pacientes.dto.PacienteResposta;
import br.edu.pucminas.hospital.pacientes.model.Paciente;
import br.edu.pucminas.hospital.pacientes.repository.PacienteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PacienteService implements ConsultaPacientes {
    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PacienteResposta criar(PacientePedido pedido) {
        var paciente = new Paciente(pedido.nome(), pedido.cpf(), pedido.dataNascimento(),
            pedido.telefone(), pedido.endereco(), pedido.email());
        return PacienteResposta.de(repository.save(paciente));
    }

    public List<PacienteResposta> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(PacienteResposta::de).toList();
    }

    public PacienteResposta consultar(Long id) {
        return PacienteResposta.de(obter(id));
    }

    @Transactional
    public PacienteResposta atualizar(Long id, PacientePedido pedido) {
        var paciente = obter(id);
        paciente.atualizar(pedido.nome(), pedido.cpf(), pedido.dataNascimento(),
            pedido.telefone(), pedido.endereco(), pedido.email());
        return PacienteResposta.de(paciente);
    }

    @Override
    public Optional<PacienteResumo> buscarPorId(Long id) {
        return repository.findById(id).map(paciente -> new PacienteResumo(paciente.getId(), paciente.getNome()));
    }

    private Paciente obter(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }
}
