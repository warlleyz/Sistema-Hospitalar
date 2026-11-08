package br.edu.pucminas.hospital.atendimentos.model;

import br.edu.pucminas.hospital.compartilhado.contratos.*;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @DiscriminatorValue("ROTINA")
public class ConsultaRotina extends Consulta {
    @Column(length = 120) private String encaminhamento;
    protected ConsultaRotina() { }
    public ConsultaRotina(Long paciente, Long profissional, LocalDateTime inicio, String motivo) {
        super(paciente, profissional, inicio, motivo);
    }
    @Override public void validarProfissional(ProfissionalResumo profissional) {
        conferirIdentidade(profissional);
        if (profissional.categoria() != CategoriaProfissional.CLINICO_GERAL)
            throw new RegraNegocioException("Consulta de rotina exige clinico geral.");
    }
    public void encaminhar(String especialidadeDestino) {
        exigirRegistroClinico();
        encaminhamento = texto(especialidadeDestino, 120, "Especialidade de destino");
    }
    public String getEncaminhamento() { return encaminhamento; }
    @Override public String modalidade() { return "ROTINA"; }
}
