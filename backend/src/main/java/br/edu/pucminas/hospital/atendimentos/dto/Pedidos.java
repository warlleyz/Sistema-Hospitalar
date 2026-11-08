package br.edu.pucminas.hospital.atendimentos.dto;

import br.edu.pucminas.hospital.atendimentos.model.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class Pedidos {
    private Pedidos() { }
    public enum TipoConsulta { ROTINA, ESPECIALIZADA }
    public record NovaConsulta(
        @NotNull TipoConsulta tipo,
        @NotNull @Positive Long pacienteId,
        @NotNull @Positive Long profissionalId,
        @NotNull LocalDateTime inicioPrevisto,
        @NotBlank @Size(max = 1000) String motivo,
        @Size(max = 120) String especialidade) { }
    public record NovaEmergencia(
        @NotNull @Positive Long pacienteId,
        @NotNull @Positive Long profissionalId,
        @NotNull LocalDateTime entrada,
        @NotBlank @Size(max = 1000) String motivo,
        @NotNull Prioridade prioridade) { }
    public record NovaCirurgia(
        @NotNull @Positive Long pacienteId,
        @NotNull @Positive Long profissionalId,
        @NotNull LocalDateTime inicioPrevisto,
        @NotBlank @Size(max = 1000) String descricao,
        @NotBlank @Size(max = 120) String tipoCirurgia,
        @Size(max = 4000) String observacoesPreOperatorias) { }
    public record Transicao(@NotNull SituacaoAtendimento situacao, LocalDateTime momento) { }
    public record RegistroClinico(
        @NotBlank @Size(max = 4000) String observacoes,
        @Size(max = 120) String encaminhamento,
        @Size(max = 4000) String observacoesPosOperatorias) { }
}
