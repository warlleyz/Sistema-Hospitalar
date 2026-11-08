package br.edu.pucminas.hospital.atendimentos.model;

import br.edu.pucminas.hospital.compartilhado.contratos.*;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AtendimentoTest {
    private final LocalDateTime inicio = LocalDateTime.of(2026, 11, 20, 10, 0);
    private ConsultaRotina rotina() { return new ConsultaRotina(1L, 2L, inicio, "Avaliacao"); }
    private ProfissionalResumo profissional(CategoriaProfissional categoria, String especialidade) {
        return new ProfissionalResumo(2L, "Medico", categoria, especialidade);
    }
    @Test void rotinaExigeClinico() {
        rotina().validarProfissional(profissional(CategoriaProfissional.CLINICO_GERAL, "Clinica"));
        assertThatThrownBy(() -> rotina().validarProfissional(profissional(CategoriaProfissional.CIRURGIAO, "Cirurgia")))
            .isInstanceOf(RegraNegocioException.class);
    }
    @Test void especializadaConfereCategoriaEEspecialidade() {
        var consulta = new ConsultaEspecializada(1L, 2L, inicio, "Revisao", "Cardiologia");
        consulta.validarProfissional(profissional(CategoriaProfissional.ESPECIALISTA, " cardiologia "));
        assertThatThrownBy(() -> consulta.validarProfissional(profissional(CategoriaProfissional.ESPECIALISTA, "Neurologia")))
            .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> consulta.validarProfissional(profissional(CategoriaProfissional.CLINICO_GERAL, "Cardiologia")))
            .isInstanceOf(RegraNegocioException.class);
    }
    @Test void consultaPercorreFluxoEImpossibilitaReabertura() {
        var c = rotina();
        c.alterarSituacao(SituacaoAtendimento.CONFIRMADA, null);
        c.alterarSituacao(SituacaoAtendimento.EM_ATENDIMENTO, inicio);
        c.registrarObservacoes("Orientacoes");
        c.encaminhar("Cardiologia");
        c.alterarSituacao(SituacaoAtendimento.CONCLUIDA, inicio.plusMinutes(30));
        assertThat(c.getEncaminhamento()).isEqualTo("Cardiologia");
        assertThat(c.getFimEfetivo()).isEqualTo(inicio.plusMinutes(30));
        assertThatThrownBy(() -> c.alterarSituacao(SituacaoAtendimento.AGENDADA, null)).isInstanceOf(RegraNegocioException.class);
    }
    @Test void cancelamentoLiberaReservaESemNovoAgendamentoNaoConclui() {
        var c = rotina();
        c.alterarSituacao(SituacaoAtendimento.CANCELADA, null);
        assertThat(c.reservaHorario()).isFalse();
        assertThatThrownBy(() -> c.alterarSituacao(SituacaoAtendimento.CONCLUIDA, inicio)).isInstanceOf(RegraNegocioException.class);
    }
    @Test void naoComparecimentoLiberaReserva() {
        var c = rotina();
        c.alterarSituacao(SituacaoAtendimento.NAO_COMPARECEU, null);
        assertThat(c.reservaHorario()).isFalse();
    }
    @Test void dadosClinicosExigemInicio() {
        assertThatThrownBy(() -> rotina().registrarObservacoes("Teste")).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> rotina().encaminhar("Cardiologia")).isInstanceOf(RegraNegocioException.class);
    }
    @Test void cirurgiaNaoConcluiSemInicioENaoAceitaClinico() {
        var c = new Cirurgia(1L, 2L, inicio, "Procedimento", "Geral", "Pre");
        assertThatThrownBy(() -> c.alterarSituacao(SituacaoAtendimento.CONCLUIDA, inicio)).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> c.validarProfissional(profissional(CategoriaProfissional.CLINICO_GERAL, "Clinica")))
            .isInstanceOf(RegraNegocioException.class);
        c.validarProfissional(profissional(CategoriaProfissional.CIRURGIAO, "Cirurgia"));
        c.alterarSituacao(SituacaoAtendimento.EM_ATENDIMENTO, inicio);
        assertThatThrownBy(() -> c.registrarPosOperatorio("Pos")).isInstanceOf(RegraNegocioException.class);
        c.alterarSituacao(SituacaoAtendimento.CONCLUIDA, inicio.plusHours(1));
        c.registrarPosOperatorio("Pos");
        assertThat(c.getObservacoesPosOperatorias()).isEqualTo("Pos");
        assertThat(c.reservaHorario()).isTrue();
    }
    @Test void emergenciaExigePrioridadeEInicioAposEntrada() {
        assertThatThrownBy(() -> new Emergencia(1L, 2L, inicio, "Dor", null)).isInstanceOf(RegraNegocioException.class);
        var e = new Emergencia(1L, 2L, inicio, "Dor", Prioridade.CRITICA);
        assertThat(e.getSituacao()).isEqualTo(SituacaoAtendimento.AGUARDANDO);
        assertThatThrownBy(() -> e.alterarSituacao(SituacaoAtendimento.EM_ATENDIMENTO, inicio.minusMinutes(1)))
            .isInstanceOf(RegraNegocioException.class);
        e.alterarSituacao(SituacaoAtendimento.EM_ATENDIMENTO, inicio);
        e.alterarSituacao(SituacaoAtendimento.CONCLUIDA, inicio.plusMinutes(10));
        assertThat(e.getPrioridade()).isEqualTo(Prioridade.CRITICA);
    }
    @Test void terminoNaoAntecedeInicioENaoAlteraEstadoEmErro() {
        var c = rotina();
        assertThatThrownBy(() -> c.alterarSituacao(SituacaoAtendimento.EM_ATENDIMENTO, null)).isInstanceOf(RegraNegocioException.class);
        assertThat(c.getSituacao()).isEqualTo(SituacaoAtendimento.AGENDADA);
        c.alterarSituacao(SituacaoAtendimento.EM_ATENDIMENTO, inicio);
        assertThatThrownBy(() -> c.alterarSituacao(SituacaoAtendimento.CONCLUIDA, inicio.minusMinutes(1)))
            .isInstanceOf(RegraNegocioException.class);
        assertThat(c.getFimEfetivo()).isNull();
    }
    @Test void protegeInvariantesMesmoSemController() {
        assertThatThrownBy(() -> new ConsultaRotina(-1L, 2L, inicio, "Motivo")).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> new ConsultaRotina(1L, 2L, inicio, " ")).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> rotina().validarProfissional(new ProfissionalResumo(99L, "Outro", CategoriaProfissional.CLINICO_GERAL, "Clinica")))
            .isInstanceOf(RegraNegocioException.class);
    }
}
