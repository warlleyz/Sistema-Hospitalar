package br.edu.pucminas.hospital.internacoes.service;

import br.edu.pucminas.hospital.internacoes.repository.InternacaoRepository;
import br.edu.pucminas.hospital.quartos.service.ConsultaOcupacaoQuartos;
import org.springframework.stereotype.Component;

@Component
public class OcupacaoInternacoes implements ConsultaOcupacaoQuartos {
    private final InternacaoRepository repository;

    public OcupacaoInternacoes(InternacaoRepository repository) { this.repository = repository; }

    @Override public long ativasNoQuarto(Long quartoId) {
        return repository.countByQuartoIdAndDataAltaEfetivaIsNull(quartoId);
    }
}
