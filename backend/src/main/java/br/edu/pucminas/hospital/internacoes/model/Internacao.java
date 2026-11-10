package br.edu.pucminas.hospital.internacoes.model;

import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "internacao")
public class Internacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private long versao;
    @Column(nullable = false) private Long pacienteId;
    @Column(nullable = false) private Long profissionalId;
    @Column(nullable = false) private Long quartoId;
    @Column(nullable = false) private LocalDate dataEntrada;
    private LocalDate dataPrevistaAlta;
    private LocalDate dataAltaEfetiva;
    @Column(length = 4000) private String observacoes;

    protected Internacao() { }

    public Internacao(Long pacienteId, Long profissionalId, Long quartoId, LocalDate dataEntrada,
        LocalDate dataPrevistaAlta, String observacoes) {
        if (pacienteId == null || pacienteId <= 0 || profissionalId == null || profissionalId <= 0
            || quartoId == null || quartoId <= 0 || dataEntrada == null)
            throw new RegraNegocioException("Paciente, profissional, quarto e entrada sao obrigatorios.");
        this.pacienteId = pacienteId;
        this.quartoId = quartoId;
        this.dataEntrada = dataEntrada;
        atualizar(profissionalId, dataPrevistaAlta, observacoes);
    }

    public void atualizar(Long profissionalId, LocalDate prevista, String observacoes) {
        exigirAtiva();
        if (profissionalId == null || profissionalId <= 0)
            throw new RegraNegocioException("Profissional responsavel invalido.");
        conferirData(prevista);
        this.profissionalId = profissionalId;
        this.dataPrevistaAlta = prevista;
        this.observacoes = normalizarObservacoes(observacoes);
    }

    public void registrarAlta(LocalDate alta) {
        exigirAtiva();
        if (alta == null || alta.isBefore(dataEntrada))
            throw new RegraNegocioException("Alta efetiva nao pode ser anterior a entrada.");
        dataAltaEfetiva = alta;
    }

    private void conferirData(LocalDate data) {
        if (data != null && data.isBefore(dataEntrada))
            throw new RegraNegocioException("Previsao de alta nao pode ser anterior a entrada.");
    }

    private void exigirAtiva() {
        if (dataAltaEfetiva != null)
            throw new RegraNegocioException("Internacao encerrada nao pode ser alterada ou receber nova alta.");
    }

    private static String normalizarObservacoes(String texto) {
        if (texto != null && texto.length() > 4000)
            throw new RegraNegocioException("Observacoes limitadas a 4000 caracteres.");
        return texto == null ? null : texto.strip();
    }

    public Long getId() { return id; }
    public Long getPacienteId() { return pacienteId; }
    public Long getProfissionalId() { return profissionalId; }
    public Long getQuartoId() { return quartoId; }
    public LocalDate getDataEntrada() { return dataEntrada; }
    public LocalDate getDataPrevistaAlta() { return dataPrevistaAlta; }
    public LocalDate getDataAltaEfetiva() { return dataAltaEfetiva; }
    public String getObservacoes() { return observacoes; }
    public boolean ativa() { return dataAltaEfetiva == null; }
}
