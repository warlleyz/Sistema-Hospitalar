package br.edu.pucminas.hospital.profissionais.model;

import br.edu.pucminas.hospital.compartilhado.contratos.CategoriaProfissional;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static br.edu.pucminas.hospital.compartilhado.contratos.CategoriaProfissional.CIRURGIAO;
import static br.edu.pucminas.hospital.compartilhado.contratos.CategoriaProfissional.ESPECIALISTA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfissionalSaudeTest {
    @ParameterizedTest
    @EnumSource(value = CategoriaProfissional.class, names = {"CLINICO_GERAL", "ESPECIALISTA"})
    void representaCategoriasNaoCirurgicasSemAreaCirurgica(CategoriaProfissional categoria) {
        var profissional = profissional(categoria, null);
        assertThat(profissional.getCategoria()).isEqualTo(categoria);
        assertThat(profissional.getEspecialidade()).isEqualTo("Cardiologia");
        assertThat(profissional.getAreaCirurgica()).isNull();

        assertThatThrownBy(() -> profissional(categoria, "Cirurgia vascular"))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void cirurgiaoPrecisaDeAreaCirurgica() {
        var cirurgiao = profissional(CIRURGIAO, " Cirurgia vascular ");
        assertThat(cirurgiao.getCategoria()).isEqualTo(CIRURGIAO);
        assertThat(cirurgiao.getAreaCirurgica()).isEqualTo("Cirurgia vascular");

        assertThatThrownBy(() -> profissional(CIRURGIAO, " "))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizacaoInvalidaPreservaCategoriaEAreaAnteriores() {
        var profissional = profissional(ESPECIALISTA, null);
        assertThatThrownBy(() -> profissional.atualizar("Outro nome", "CRM-MG 100", CIRURGIAO,
            "Vascular", null, "(31) 99999-0000", "equipe@example.org"))
            .isInstanceOf(RegraNegocioException.class);
        assertThat(profissional.getNome()).isEqualTo("Profissional Exemplo");
        assertThat(profissional.getCategoria()).isEqualTo(ESPECIALISTA);
        assertThat(profissional.getAreaCirurgica()).isNull();
    }

    private ProfissionalSaude profissional(CategoriaProfissional categoria, String areaCirurgica) {
        return new ProfissionalSaude("Profissional Exemplo", "CRM-MG 100", categoria,
            "Cardiologia", areaCirurgica, "(31) 99999-0000", "equipe@example.org");
    }
}
