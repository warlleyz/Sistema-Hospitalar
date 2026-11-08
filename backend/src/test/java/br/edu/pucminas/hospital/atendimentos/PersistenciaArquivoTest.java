package br.edu.pucminas.hospital.atendimentos;

import br.edu.pucminas.hospital.HospitalApplication;
import br.edu.pucminas.hospital.atendimentos.dto.Pedidos.*;
import br.edu.pucminas.hospital.atendimentos.service.AtendimentoService;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import static org.assertj.core.api.Assertions.*;

class PersistenciaArquivoTest {
    private ConfigurableApplicationContext iniciar(String url) {
        return new SpringApplicationBuilder(HospitalApplication.class, CadastrosTestConfig.class)
            .web(WebApplicationType.NONE).run("--spring.datasource.url=" + url,
                "--spring.main.banner-mode=off", "--logging.level.root=WARN");
    }
    @Test void atendimentoEReservaSobrevivemAoReinicio() {
        String url = "jdbc:h2:file:./target/persistencia-" + UUID.randomUUID() + ";DB_CLOSE_ON_EXIT=FALSE";
        var pedido = new NovaConsulta(TipoConsulta.ROTINA, 1L, 10L,
            LocalDateTime.of(2026, 11, 20, 10, 0), "Persistencia", null);
        Long id;
        try (var contexto = iniciar(url)) {
            id = contexto.getBean(AtendimentoService.class).criarConsulta(pedido).id();
        }
        try (var contexto = iniciar(url)) {
            var service = contexto.getBean(AtendimentoService.class);
            assertThat(service.consultar("consultas", id).motivo()).isEqualTo("Persistencia");
            assertThat(service.disponivel(10L, pedido.inicioPrevisto())).isFalse();
            assertThatThrownBy(() -> service.criarConsulta(pedido)).isInstanceOf(RegraNegocioException.class);
        }
    }
}
