package br.edu.pucminas.hospital.atendimentos.service;

import br.edu.pucminas.hospital.atendimentos.dto.*;
import br.edu.pucminas.hospital.atendimentos.dto.Pedidos.*;
import br.edu.pucminas.hospital.atendimentos.model.*;
import br.edu.pucminas.hospital.atendimentos.repository.*;
import br.edu.pucminas.hospital.compartilhado.contratos.Cadastros;
import br.edu.pucminas.hospital.compartilhado.contratos.ConsultaDisponibilidade;
import br.edu.pucminas.hospital.compartilhado.erros.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AtendimentoService implements ConsultaDisponibilidade {
    private final AtendimentoRepository repository;
    private final AgendaRepository agenda;
    private final Cadastros cadastros;
    public AtendimentoService(AtendimentoRepository repository, AgendaRepository agenda, Cadastros cadastros) {
        this.repository = repository; this.agenda = agenda; this.cadastros = cadastros;
    }
    public AtendimentoResposta criarConsulta(NovaConsulta p) {
        if (p.tipo() == null) throw new RegraNegocioException("Tipo de consulta obrigatorio.");
        if (p.tipo() == TipoConsulta.ROTINA && p.especialidade() != null)
            throw new RegraNegocioException("Especialidade solicitada pertence a consulta especializada.");
        Atendimento a = p.tipo() == TipoConsulta.ROTINA
            ? new ConsultaRotina(p.pacienteId(), p.profissionalId(), p.inicioPrevisto(), p.motivo())
            : new ConsultaEspecializada(p.pacienteId(), p.profissionalId(), p.inicioPrevisto(), p.motivo(), p.especialidade());
        return criar(a);
    }
    public AtendimentoResposta criarEmergencia(NovaEmergencia p) {
        return criar(new Emergencia(p.pacienteId(), p.profissionalId(), p.entrada(), p.motivo(), p.prioridade()));
    }
    public AtendimentoResposta criarCirurgia(NovaCirurgia p) {
        return criar(new Cirurgia(p.pacienteId(), p.profissionalId(), p.inicioPrevisto(), p.descricao(), p.tipoCirurgia(), p.observacoesPreOperatorias()));
    }
    private AtendimentoResposta criar(Atendimento a) {
        agenda.bloquear();
        cadastros.paciente(a.getPacienteId());
        a.validarProfissional(cadastros.profissional(a.getProfissionalId()));
        if (agenda.ocupado(a.getProfissionalId(), a.getInicioPrevisto()))
            throw new RegraNegocioException("Profissional ja possui atendimento neste horario.");
        repository.saveAndFlush(a);
        agenda.reservar(a.getId(), a.getProfissionalId(), a.getInicioPrevisto());
        return AtendimentoResposta.de(a);
    }
    @Transactional(readOnly = true)
    public List<AtendimentoResposta> listar(String recurso, Long paciente, SituacaoAtendimento situacao) {
        if (paciente != null) cadastros.paciente(paciente);
        var registros = paciente == null ? repository.findAllByOrderByInicioPrevistoAscIdAsc()
            : repository.findByPacienteIdOrderByInicioPrevistoAscIdAsc(paciente);
        return registros.stream().filter(a -> pertence(a, recurso))
            .filter(a -> situacao == null || situacao == a.getSituacao()).map(AtendimentoResposta::de).toList();
    }
    @Transactional(readOnly = true)
    public AtendimentoResposta consultar(String recurso, Long id) { return AtendimentoResposta.de(obter(recurso, id)); }
    public AtendimentoResposta transicionar(String recurso, Long id, Transicao p) {
        agenda.bloquear();
        var a = obter(recurso, id);
        if (p.situacao() == SituacaoAtendimento.EM_ATENDIMENTO) {
            cadastros.paciente(a.getPacienteId());
            a.validarProfissional(cadastros.profissional(a.getProfissionalId()));
        }
        a.alterarSituacao(p.situacao(), p.momento());
        if (!a.reservaHorario()) agenda.liberar(id);
        repository.flush();
        return AtendimentoResposta.de(a);
    }
    public AtendimentoResposta registrar(String recurso, Long id, RegistroClinico p) {
        agenda.bloquear();
        var a = obter(recurso, id);
        a.registrarObservacoes(p.observacoes());
        if (p.encaminhamento() != null) {
            if (!(a instanceof ConsultaRotina r)) throw new RegraNegocioException("Encaminhamento exige consulta de rotina.");
            r.encaminhar(p.encaminhamento());
        }
        if (p.observacoesPosOperatorias() != null) {
            if (!(a instanceof Cirurgia c)) throw new RegraNegocioException("Pos-operatorio exige cirurgia.");
            c.registrarPosOperatorio(p.observacoesPosOperatorias());
        }
        repository.flush();
        return AtendimentoResposta.de(a);
    }
    @Transactional(readOnly = true)
    public boolean disponivel(Long profissional, LocalDateTime inicio) {
        cadastros.profissional(profissional);
        return !agenda.ocupado(profissional, inicio);
    }
    private Atendimento obter(String recurso, Long id) {
        return repository.findById(id).filter(a -> pertence(a, recurso))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento nao encontrado neste recurso."));
    }
    private boolean pertence(Atendimento a, String recurso) {
        return switch (recurso) {
            case "consultas" -> a instanceof Consulta;
            case "emergencias" -> a instanceof Emergencia;
            case "cirurgias" -> a instanceof Cirurgia;
            default -> false;
        };
    }
}
