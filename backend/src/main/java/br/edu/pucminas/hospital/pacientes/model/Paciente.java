package br.edu.pucminas.hospital.pacientes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.time.LocalDate;

@Entity
public class Paciente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long versao;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, length = 30)
    private String telefone;

    @Column(nullable = false, length = 255)
    private String endereco;

    @Column(nullable = false, length = 254)
    private String email;

    protected Paciente() { }

    public Paciente(String nome, String cpf, LocalDate dataNascimento, String telefone, String endereco, String email) {
        atualizar(nome, cpf, dataNascimento, telefone, endereco, email);
    }

    public void atualizar(String nome, String cpf, LocalDate dataNascimento, String telefone, String endereco, String email) {
        this.nome = nome.trim();
        this.cpf = cpf.trim().replace(".", "").replace("-", "");
        this.dataNascimento = dataNascimento;
        this.telefone = telefone.trim();
        this.endereco = endereco.trim();
        this.email = email.trim();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getTelefone() { return telefone; }
    public String getEndereco() { return endereco; }
    public String getEmail() { return email; }
}
