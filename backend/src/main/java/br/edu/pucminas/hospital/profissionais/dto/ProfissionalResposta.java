package br.edu.pucminas.hospital.profissionais.dto;

import br.edu.pucminas.hospital.compartilhado.contratos.CategoriaProfissional;
import br.edu.pucminas.hospital.profissionais.model.ProfissionalSaude;

public record ProfissionalResposta(Long id, String nome, String registroProfissional, CategoriaProfissional categoria,
                                   String especialidade, String areaCirurgica, String telefone, String email) {
    public static ProfissionalResposta de(ProfissionalSaude profissional) {
        return new ProfissionalResposta(profissional.getId(), profissional.getNome(),
            profissional.getRegistroProfissional(), profissional.getCategoria(), profissional.getEspecialidade(),
            profissional.getAreaCirurgica(), profissional.getTelefone(), profissional.getEmail());
    }
}
