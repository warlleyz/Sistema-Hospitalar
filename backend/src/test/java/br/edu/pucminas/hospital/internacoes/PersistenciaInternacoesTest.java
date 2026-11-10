package br.edu.pucminas.hospital.internacoes;

import br.edu.pucminas.hospital.HospitalApplication;
import br.edu.pucminas.hospital.internacoes.dto.PedidosInternacao.*;
import br.edu.pucminas.hospital.internacoes.service.InternacaoService;
import br.edu.pucminas.hospital.quartos.dto.PedidosQuarto.NovoQuarto;
import br.edu.pucminas.hospital.quartos.service.QuartoService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import static org.assertj.core.api.Assertions.*;

class PersistenciaInternacoesTest {
    private ConfigurableApplicationContext iniciar(String url) {
        return new SpringApplicationBuilder(HospitalApplication.class, CadastrosInternacoesTestConfig.class)
            .web(WebApplicationType.NONE).run("--spring.datasource.url=" + url,
                "--spring.main.banner-mode=off", "--logging.level.root=WARN");
    }

    @Test void quartosInternacoesEAltaPersistemEntreReinicios() {
        String url = "jdbc:h2:file:./target/persistencia-internacoes-" + UUID.randomUUID()
            + ";DB_CLOSE_ON_EXIT=FALSE";
        LocalDate entrada = LocalDate.of(2026, 11, 20);
        Long quartoId;
        Long internacaoId;

        try (var contexto = iniciar(url)) {
            var quartos = contexto.getBean(QuartoService.class);
            var internacoes = contexto.getBean(InternacaoService.class);
            quartoId = quartos.cadastrar(new NovoQuarto("Persistencia-1", 2, 1)).id();
            internacaoId = internacoes.registrarEntrada(new NovaInternacao(1L, 10L, quartoId, entrada,
                entrada.plusDays(3), "Observacao preservada")).id();
        }

        try (var contexto = iniciar(url)) {
            var quartos = contexto.getBean(QuartoService.class);
            var internacoes = contexto.getBean(InternacaoService.class);
            assertThat(quartos.consultar(quartoId).vagasDisponiveis()).isZero();
            assertThat(internacoes.consultar(internacaoId).observacoes()).isEqualTo("Observacao preservada");
            internacoes.registrarAlta(internacaoId, new AltaInternacao(entrada.plusDays(2)));
        }

        try (var contexto = iniciar(url)) {
            var quartos = contexto.getBean(QuartoService.class);
            var internacoes = contexto.getBean(InternacaoService.class);
            assertThat(quartos.consultar(quartoId).vagasDisponiveis()).isEqualTo(1);
            assertThat(internacoes.listar(1L)).hasSize(1);
            assertThat(internacoes.consultar(internacaoId).dataAltaEfetiva()).isEqualTo(entrada.plusDays(2));
        }
    }
}
