package br.edu.pucminas.hospital.compartilhado.contratos;

import br.edu.pucminas.hospital.compartilhado.erros.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Ponte sem cadastros ficticios: permite iniciar enquanto os modulos estao separados. */
@Component
public class Cadastros {
    private final ObjectProvider<ConsultaPacientes> pacientes;
    private final ObjectProvider<ConsultaProfissionais> profissionais;

    public Cadastros(ObjectProvider<ConsultaPacientes> pacientes, ObjectProvider<ConsultaProfissionais> profissionais) {
        this.pacientes = pacientes;
        this.profissionais = profissionais;
    }

    public PacienteResumo paciente(Long id) {
        var consulta = pacientes.getIfAvailable();
        if (consulta == null) throw new DependenciaIndisponivelException("Modulo de pacientes ainda nao integrado.");
        return consulta.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado."));
    }

    public ProfissionalResumo profissional(Long id) {
        var consulta = profissionais.getIfAvailable();
        if (consulta == null) throw new DependenciaIndisponivelException("Modulo de profissionais ainda nao integrado.");
        return consulta.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Profissional nao encontrado."));
    }
}
