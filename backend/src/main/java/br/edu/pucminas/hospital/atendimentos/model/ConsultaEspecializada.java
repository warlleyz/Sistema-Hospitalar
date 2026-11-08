package br.edu.pucminas.hospital.atendimentos.model;

import br.edu.pucminas.hospital.compartilhado.contratos.*;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @DiscriminatorValue("ESPECIALIZADA")
public class ConsultaEspecializada extends Consulta {
    @Column(length = 120) private String especialidade;
    protected ConsultaEspecializada() { }
    public ConsultaEspecializada(Long paciente, Long profissional, LocalDateTime inicio, String motivo, String especialidade) {
        super(paciente, profissional, inicio, motivo);
        this.especialidade = texto(especialidade, 120, "Especialidade");
    }
    @Override public void validarProfissional(ProfissionalResumo profissional) {
        conferirIdentidade(profissional);
        if (profissional.categoria() != CategoriaProfissional.ESPECIALISTA || profissional.especialidade() == null
            || !especialidade.equalsIgnoreCase(profissional.especialidade().trim()))
            throw new RegraNegocioException("Especialidade do profissional incompativel.");
    }
    public String getEspecialidade() { return especialidade; }
    @Override public String modalidade() { return "ESPECIALIZADA"; }
}
