package br.edu.pucminas.hospital.historico;

import br.edu.pucminas.hospital.atendimentos.service.AtendimentoService;
import br.edu.pucminas.hospital.internacoes.service.InternacaoService;
import br.edu.pucminas.hospital.compartilhado.contratos.Cadastros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
public class HistoricoService {
    private final Cadastros cadastros;
    private final AtendimentoService atendimentos;
    private final InternacaoService internacoes;

    public HistoricoService(Cadastros cadastros, AtendimentoService atendimentos, InternacaoService internacoes) {
        this.cadastros = cadastros;
        this.atendimentos = atendimentos;
        this.internacoes = internacoes;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public HistoricoMedico consultar(Long pacienteId) {
        return new HistoricoMedico(cadastros.paciente(pacienteId),
            atendimentos.historicoDoPaciente(pacienteId), internacoes.listar(pacienteId));
    }
}
