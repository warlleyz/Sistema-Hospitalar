package br.edu.pucminas.hospital.quartos.service;

import br.edu.pucminas.hospital.compartilhado.erros.*;
import br.edu.pucminas.hospital.quartos.dto.*;
import br.edu.pucminas.hospital.quartos.dto.PedidosQuarto.*;
import br.edu.pucminas.hospital.quartos.model.Quarto;
import br.edu.pucminas.hospital.quartos.repository.QuartoRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuartoService {
    private final QuartoRepository repository;
    private final ConsultaOcupacaoQuartos ocupacao;

    public QuartoService(QuartoRepository repository, ConsultaOcupacaoQuartos ocupacao) {
        this.repository = repository;
        this.ocupacao = ocupacao;
    }

    public QuartoResposta cadastrar(NovoQuarto pedido) {
        conferirNumeroLivre(pedido.numero(), null);
        var quarto = repository.saveAndFlush(new Quarto(pedido.numero(), pedido.andar(), pedido.capacidadeMaxima()));
        return QuartoResposta.de(quarto, 0);
    }

    @Transactional(readOnly = true)
    public List<QuartoResposta> listar() {
        return repository.findAllByOrderByNumeroAscIdAsc().stream().map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public QuartoResposta consultar(Long id) {
        return resposta(repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto nao encontrado.")));
    }

    public QuartoResposta atualizar(Long id, AtualizarQuarto pedido) {
        var quarto = obterBloqueado(id);
        long ativas = ocupacao.ativasNoQuarto(id);
        if (pedido.capacidadeMaxima() < ativas)
            throw new RegraNegocioException("Capacidade nao pode ser menor que a ocupacao atual.");
        conferirNumeroLivre(pedido.numero(), quarto.getNumero());
        quarto.atualizar(pedido.numero(), pedido.andar(), pedido.capacidadeMaxima());
        repository.flush();
        return QuartoResposta.de(quarto, ativas);
    }

    /** Mesma linha bloqueada em entrada, alta e alteracao de capacidade. */
    public void exigirVaga(Long quartoId) {
        var quarto = obterBloqueado(quartoId);
        if (ocupacao.ativasNoQuarto(quartoId) >= quarto.getCapacidadeMaxima())
            throw new RegraNegocioException("Quarto sem vagas disponiveis.");
    }

    public void bloquearExistente(Long quartoId) { obterBloqueado(quartoId); }

    private Quarto obterBloqueado(Long id) {
        return repository.bloquear(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto nao encontrado."));
    }

    private QuartoResposta resposta(Quarto quarto) {
        return QuartoResposta.de(quarto, ocupacao.ativasNoQuarto(quarto.getId()));
    }

    private void conferirNumeroLivre(String numero, String atual) {
        if (numero == null || numero.isBlank() || numero.strip().length() > 32)
            throw new RegraNegocioException("Numero do quarto invalido.");
        String normalizado = numero.strip().toUpperCase(Locale.ROOT);
        if (!normalizado.equals(atual) && repository.existsByNumero(normalizado))
            throw new RegraNegocioException("Numero de quarto ja cadastrado.");
    }
}
