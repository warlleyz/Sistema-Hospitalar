package br.edu.pucminas.hospital.internacoes;

import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import br.edu.pucminas.hospital.internacoes.dto.PedidosInternacao.*;
import br.edu.pucminas.hospital.internacoes.service.InternacaoService;
import br.edu.pucminas.hospital.quartos.dto.PedidosQuarto.*;
import br.edu.pucminas.hospital.quartos.service.QuartoService;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:concorrencia-internacoes;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000")
@Import(CadastrosInternacoesTestConfig.class)
class ConcorrenciaInternacoesTest {
    @Autowired QuartoService quartos;
    @Autowired InternacaoService internacoes;
    @Autowired JdbcTemplate jdbc;
    private final LocalDate entrada = LocalDate.of(2026, 11, 20);

    @BeforeEach void limpar() {
        jdbc.update("DELETE FROM internacao");
        jdbc.update("DELETE FROM quarto");
    }

    private NovoQuarto quarto(String numero, int capacidade) { return new NovoQuarto(numero, 1, capacidade); }

    private NovaInternacao pedido(long paciente, long profissional, long quartoId) {
        return new NovaInternacao(paciente, profissional, quartoId, entrada, entrada.plusDays(3), "Concorrente");
    }

    private List<Boolean> disputar(Callable<Boolean> primeira, Callable<Boolean> segunda) throws Exception {
        var prontos = new CountDownLatch(2);
        var largada = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        Callable<Boolean> sincronizada1 = () -> {
            prontos.countDown();
            if (!largada.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Sem largada");
            return primeira.call();
        };
        Callable<Boolean> sincronizada2 = () -> {
            prontos.countDown();
            if (!largada.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Sem largada");
            return segunda.call();
        };
        try {
            var a = pool.submit(sincronizada1);
            var b = pool.submit(sincronizada2);
            assertThat(prontos.await(5, TimeUnit.SECONDS)).isTrue();
            largada.countDown();
            return List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        } finally {
            largada.countDown();
            pool.shutdownNow();
        }
    }

    @Test void duasEntradasConcorrentesNaoOcupamUltimaVagaDuasVezes() throws Exception {
        long id = quartos.cadastrar(quarto("C-1", 1)).id();
        var resultados = disputar(() -> tentarEntrada(pedido(1, 10, id)), () -> tentarEntrada(pedido(2, 11, id)));
        assertThat(resultados).containsExactlyInAnyOrder(true, false);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM internacao WHERE data_alta_efetiva IS NULL", Long.class))
            .isEqualTo(1);
        assertThat(quartos.consultar(id).ocupacaoAtual()).isEqualTo(1);
        assertThat(quartos.consultar(id).vagasDisponiveis()).isZero();
    }

    @Test void reducaoDeCapacidadeEEntradaNaoProduzemSuperlotacao() throws Exception {
        long id = quartos.cadastrar(quarto("C-2", 2)).id();
        internacoes.registrarEntrada(pedido(1, 10, id));
        var resultados = disputar(() -> {
            try { quartos.atualizar(id, new AtualizarQuarto("C-2", 1, 1)); return true; }
            catch (RegraNegocioException conflito) { return false; }
        }, () -> tentarEntrada(pedido(2, 11, id)));
        assertThat(resultados).containsExactlyInAnyOrder(true, false);
        var quartoFinal = quartos.consultar(id);
        assertThat(quartoFinal.ocupacaoAtual()).isLessThanOrEqualTo(quartoFinal.capacidadeMaxima());
        assertThat(quartoFinal.vagasDisponiveis()).isZero();
        assertThat(List.of(quartoFinal.capacidadeMaxima(), (int) quartoFinal.ocupacaoAtual()))
            .isIn(List.of(1, 1), List.of(2, 2));
    }

    @Test void altaConcorrenteAEntradaMantemVagasEHistoricoConsistentes() throws Exception {
        long id = quartos.cadastrar(quarto("C-3", 1)).id();
        long internaId = internacoes.registrarEntrada(pedido(1, 10, id)).id();
        var resultados = disputar(() -> {
            internacoes.registrarAlta(internaId, new AltaInternacao(entrada.plusDays(1)));
            return true;
        }, () -> tentarEntrada(pedido(2, 11, id)));
        assertThat(resultados.get(0)).isTrue();
        var resposta = quartos.consultar(id);
        assertThat(resposta.ocupacaoAtual()).isBetween(0L, 1L);
        assertThat(resposta.vagasDisponiveis()).isEqualTo(1 - resposta.ocupacaoAtual());
        assertThat(internacoes.consultar(internaId).dataAltaEfetiva()).isEqualTo(entrada.plusDays(1));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM internacao", Long.class))
            .isEqualTo(resultados.get(1) ? 2 : 1);
    }

    private boolean tentarEntrada(NovaInternacao pedido) {
        try { internacoes.registrarEntrada(pedido); return true; }
        catch (RegraNegocioException conflito) { return false; }
    }
}
