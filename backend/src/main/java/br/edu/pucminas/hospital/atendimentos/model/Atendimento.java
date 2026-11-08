package br.edu.pucminas.hospital.atendimentos.model;

import br.edu.pucminas.hospital.compartilhado.contratos.ProfissionalResumo;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "atendimento")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "modalidade")
public abstract class Atendimento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private long versao;
    @Column(nullable = false) private Long pacienteId;
    @Column(nullable = false) private Long profissionalId;
    @Column(nullable = false) private LocalDateTime inicioPrevisto;
    @Column(nullable = false, length = 1000) private String motivo;
    @Column(length = 4000) private String observacoes;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private SituacaoAtendimento situacao;
    private LocalDateTime inicioEfetivo;
    private LocalDateTime fimEfetivo;

    protected Atendimento() { }
    protected Atendimento(Long pacienteId, Long profissionalId, LocalDateTime inicio, String motivo, SituacaoAtendimento situacao) {
        if (pacienteId == null || pacienteId <= 0 || profissionalId == null || profissionalId <= 0 || inicio == null)
            throw new RegraNegocioException("Paciente, profissional e inicio sao obrigatorios.");
        this.pacienteId = pacienteId;
        this.profissionalId = profissionalId;
        this.inicioPrevisto = inicio;
        this.motivo = texto(motivo, 1000, "Motivo");
        this.situacao = situacao;
    }
    protected static String texto(String valor, int limite, String nome) {
        if (valor == null || valor.isBlank() || valor.length() > limite)
            throw new RegraNegocioException(nome + " obrigatorio e limitado a " + limite + " caracteres.");
        return valor.trim();
    }
    public abstract void validarProfissional(ProfissionalResumo profissional);
    protected abstract Set<SituacaoAtendimento> destinosPermitidos();
    public abstract String modalidade();

    protected void conferirIdentidade(ProfissionalResumo profissional) {
        if (profissional == null || !profissionalId.equals(profissional.id()) || profissional.categoria() == null)
            throw new RegraNegocioException("Profissional incompativel com o atendimento.");
    }
    public void alterarSituacao(SituacaoAtendimento destino, LocalDateTime momento) {
        if (destino == null || !destinosPermitidos().contains(destino))
            throw new RegraNegocioException("Transicao de situacao nao permitida.");
        if (destino == SituacaoAtendimento.EM_ATENDIMENTO) {
            if (momento == null) throw new RegraNegocioException("Informe o inicio efetivo.");
            inicioEfetivo = momento;
        }
        if (destino == SituacaoAtendimento.CONCLUIDA) {
            if (inicioEfetivo == null || momento == null || momento.isBefore(inicioEfetivo))
                throw new RegraNegocioException("Conclusao exige inicio e termino nao anterior ao inicio.");
            fimEfetivo = momento;
        }
        situacao = destino;
    }
    public void registrarObservacoes(String valor) {
        exigirRegistroClinico();
        observacoes = texto(valor, 4000, "Observacoes");
    }
    protected void exigirRegistroClinico() {
        if (situacao != SituacaoAtendimento.EM_ATENDIMENTO && situacao != SituacaoAtendimento.CONCLUIDA)
            throw new RegraNegocioException("Registro clinico exige atendimento iniciado.");
    }
    public boolean reservaHorario() {
        return situacao != SituacaoAtendimento.CANCELADA && situacao != SituacaoAtendimento.NAO_COMPARECEU;
    }
    public Long getId() { return id; }
    public Long getPacienteId() { return pacienteId; }
    public Long getProfissionalId() { return profissionalId; }
    public LocalDateTime getInicioPrevisto() { return inicioPrevisto; }
    public LocalDateTime getInicioEfetivo() { return inicioEfetivo; }
    public LocalDateTime getFimEfetivo() { return fimEfetivo; }
    public String getMotivo() { return motivo; }
    public String getObservacoes() { return observacoes; }
    public SituacaoAtendimento getSituacao() { return situacao; }
}
