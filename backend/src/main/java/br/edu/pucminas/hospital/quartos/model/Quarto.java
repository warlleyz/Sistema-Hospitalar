package br.edu.pucminas.hospital.quartos.model;

import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import jakarta.persistence.*;
import java.util.Locale;

@Entity
@Table(name = "quarto")
public class Quarto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version private long versao;
    @Column(nullable = false, length = 32, unique = true)
    private String numero;
    @Column(nullable = false)
    private Integer andar;
    @Column(nullable = false)
    private Integer capacidadeMaxima;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoQuarto tipo;

    protected Quarto() { }

    public Quarto(String numero, Integer andar, Integer capacidadeMaxima) {
        this(numero, andar, capacidadeMaxima, null);
    }

    public Quarto(String numero, Integer andar, Integer capacidadeMaxima, TipoQuarto tipo) {
        atualizar(numero, andar, capacidadeMaxima, tipo);
    }

    public void atualizar(String numero, Integer andar, Integer capacidadeMaxima) {
        atualizar(numero, andar, capacidadeMaxima, null);
    }

    public void atualizar(String numero, Integer andar, Integer capacidadeMaxima, TipoQuarto tipo) {
        if (numero == null || numero.isBlank() || numero.strip().length() > 32)
            throw new RegraNegocioException("Numero do quarto obrigatorio e limitado a 32 caracteres.");
        if (andar == null) throw new RegraNegocioException("Andar obrigatorio.");
        if (capacidadeMaxima == null || capacidadeMaxima <= 0)
            throw new RegraNegocioException("Capacidade do quarto deve ser positiva.");
        TipoQuarto escolhido = tipo == null ? TipoQuarto.porCapacidade(capacidadeMaxima) : tipo;
        escolhido.validarCapacidade(capacidadeMaxima);
        this.numero = numero.strip().toUpperCase(Locale.ROOT);
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
        this.tipo = escolhido;
    }

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public Integer getAndar() { return andar; }
    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public TipoQuarto getTipo() { return tipo; }
}
