package br.edu.pucminas.hospital.historico;

import br.edu.pucminas.hospital.compartilhado.contratos.PacienteResumo;
import br.edu.pucminas.hospital.atendimentos.dto.AtendimentoResposta;
import br.edu.pucminas.hospital.internacoes.dto.InternacaoResposta;
import java.util.List;

/** Visao calculada dos registros originais: nao mantem copia de prontuario no banco. */
public record HistoricoMedico(PacienteResumo paciente, List<AtendimentoResposta> atendimentos,
    List<InternacaoResposta> internacoes) { }
