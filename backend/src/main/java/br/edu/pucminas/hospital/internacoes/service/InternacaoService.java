package br.edu.pucminas.hospital.internacoes.service;

import br.edu.pucminas.hospital.compartilhado.contratos.Cadastros;
import br.edu.pucminas.hospital.compartilhado.erros.RecursoNaoEncontradoException;
import br.edu.pucminas.hospital.internacoes.dto.*;
import br.edu.pucminas.hospital.internacoes.dto.PedidosInternacao.*;
import br.edu.pucminas.hospital.internacoes.model.Internacao;
import br.edu.pucminas.hospital.internacoes.repository.InternacaoRepository;
import br.edu.pucminas.hospital.quartos.service.QuartoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InternacaoService {
    private final InternacaoRepository repository;
    private final QuartoService quartos;
    private final Cadastros cadastros;

    public InternacaoService(InternacaoRepository repository, QuartoService quartos, Cadastros cadastros) {
        this.repository = repository;
        this.quartos = quartos;
        this.cadastros = cadastros;
    }

    public InternacaoResposta registrarEntrada(NovaInternacao pedido) {
        cadastros.paciente(pedido.pacienteId());
        cadastros.profissional(pedido.profissionalId());
        quartos.exigirVaga(pedido.quartoId());
        var internacao = new Internacao(pedido.pacienteId(), pedido.profissionalId(), pedido.quartoId(),
            pedido.dataEntrada(), pedido.dataPrevistaAlta(), pedido.observacoes());
        return InternacaoResposta.de(repository.saveAndFlush(internacao));
    }

    @Transactional(readOnly = true)
    public List<InternacaoResposta> listar(Long pacienteId) {
        if (pacienteId != null) cadastros.paciente(pacienteId);
        var registros = pacienteId == null ? repository.findAllByOrderByDataEntradaAscIdAsc()
            : repository.findByPacienteIdOrderByDataEntradaAscIdAsc(pacienteId);
        return registros.stream().map(InternacaoResposta::de).toList();
    }

    @Transactional(readOnly = true)
    public InternacaoResposta consultar(Long id) {
        return InternacaoResposta.de(obter(id));
    }

    public InternacaoResposta atualizar(Long id, AtualizarInternacao pedido) {
        var internacao = obterAposBloqueio(id);
        cadastros.profissional(pedido.profissionalId());
        internacao.atualizar(pedido.profissionalId(), pedido.dataPrevistaAlta(), pedido.observacoes());
        repository.flush();
        return InternacaoResposta.de(internacao);
    }

    public InternacaoResposta registrarAlta(Long id, AltaInternacao pedido) {
        var internacao = obterAposBloqueio(id);
        internacao.registrarAlta(pedido.dataAltaEfetiva());
        // A mesma transacao persiste a alta e remove o registro do calculo de ocupacao.
        repository.flush();
        return InternacaoResposta.de(internacao);
    }

    private Internacao obterAposBloqueio(Long id) {
        Long quartoId = repository.obterQuartoId(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Internacao nao encontrada."));
        quartos.bloquearExistente(quartoId);
        return obter(id);
    }

    private Internacao obter(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Internacao nao encontrada."));
    }
}
