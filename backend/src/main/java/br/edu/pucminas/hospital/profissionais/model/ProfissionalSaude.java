package br.edu.pucminas.hospital.profissionais.model;

import br.edu.pucminas.hospital.compartilhado.contratos.CategoriaProfissional;
import br.edu.pucminas.hospital.compartilhado.erros.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class ProfissionalSaude {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long versao;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 60)
    private String registroProfissional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaProfissional categoria;

    @Column(nullable = false, length = 120)
    private String especialidade;

    @Column(length = 120)
    private String areaCirurgica;

    @Column(nullable = false, length = 30)
    private String telefone;

    @Column(nullable = false, length = 254)
    private String email;

    protected ProfissionalSaude() { }

    public ProfissionalSaude(String nome, String registroProfissional, CategoriaProfissional categoria,
                             String especialidade, String areaCirurgica, String telefone, String email) {
        atualizar(nome, registroProfissional, categoria, especialidade, areaCirurgica, telefone, email);
    }

    public void atualizar(String nome, String registroProfissional, CategoriaProfissional categoria,
                          String especialidade, String areaCirurgica, String telefone, String email) {
        String area = areaCirurgica == null || areaCirurgica.isBlank() ? null : areaCirurgica.trim();
        if (categoria == CategoriaProfissional.CIRURGIAO && area == null) {
            throw new RegraNegocioException("Informe a área cirúrgica do cirurgião.");
        }
        if (categoria != CategoriaProfissional.CIRURGIAO && area != null) {
            throw new RegraNegocioException("A área cirúrgica é exclusiva da categoria cirurgião.");
        }

        this.nome = nome.trim();
        this.registroProfissional = registroProfissional;
        this.categoria = categoria;
        this.especialidade = especialidade.trim();
        this.areaCirurgica = area;
        this.telefone = telefone.trim();
        this.email = email.trim();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getRegistroProfissional() { return registroProfissional; }
    public CategoriaProfissional getCategoria() { return categoria; }
    public String getEspecialidade() { return especialidade; }
    public String getAreaCirurgica() { return areaCirurgica; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
}
