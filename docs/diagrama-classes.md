# Diagrama de classes consolidado — Sprint 2

Modelo do código integrado. As associações por ID representam vínculos de domínio;
entre módulos, a implementação usa contratos de consulta e não associações JPA.
O [modelo da Sprint 1](sprint-1/diagrama-classes.svg) está preservado para comparação.

```mermaid
classDiagram
    class Paciente {
        -Long id
        -long versao
        -String nome
        -String cpf
        -LocalDate dataNascimento
        -String telefone
        -String endereco
        -String email
        +atualizar()
    }
    class ProfissionalSaude {
        -Long id
        -long versao
        -String nome
        -String registroProfissional
        -CategoriaProfissional categoria
        -String especialidade
        -String areaCirurgica
        -String telefone
        -String email
        +atualizar()
    }
    class CategoriaProfissional {
        <<enumeration>>
        CLINICO_GERAL
        ESPECIALISTA
        CIRURGIAO
    }
    class Atendimento {
        <<abstract>>
        -Long id
        -long versao
        -Long pacienteId
        -Long profissionalId
        -LocalDateTime inicioPrevisto
        -LocalDateTime inicioEfetivo
        -LocalDateTime fimEfetivo
        -String motivo
        -String observacoes
        -SituacaoAtendimento situacao
        +validarProfissional(ProfissionalResumo)
        +alterarSituacao(SituacaoAtendimento, LocalDateTime)
        +registrarObservacoes(String)
        +reservaHorario() boolean
        +modalidade() String
    }
    class Consulta {
        <<abstract>>
        #destinosPermitidos() Set
    }
    class ConsultaRotina {
        -String encaminhamento
        +encaminhar(String)
        +validarProfissional(ProfissionalResumo)
    }
    class ConsultaEspecializada {
        -String especialidade
        +validarProfissional(ProfissionalResumo)
    }
    class Emergencia {
        -Prioridade prioridade
        +validarProfissional(ProfissionalResumo)
        +alterarSituacao(SituacaoAtendimento, LocalDateTime)
    }
    class Cirurgia {
        -String tipoCirurgia
        -String observacoesPreOperatorias
        -String observacoesPosOperatorias
        +registrarPosOperatorio(String)
        +validarProfissional(ProfissionalResumo)
    }
    class SituacaoAtendimento {
        <<enumeration>>
        AGENDADA
        CONFIRMADA
        AGUARDANDO
        EM_ATENDIMENTO
        CONCLUIDA
        CANCELADA
        NAO_COMPARECEU
    }
    class Prioridade {
        <<enumeration>>
        BAIXA
        MODERADA
        ALTA
        CRITICA
    }
    class Quarto {
        -Long id
        -long versao
        -String numero
        -Integer andar
        -Integer capacidadeMaxima
        -TipoQuarto tipo
        +atualizar()
    }
    class TipoQuarto {
        <<enumeration>>
        INDIVIDUAL
        COLETIVO
        +validarCapacidade(int)
        +porCapacidade(int) TipoQuarto
    }
    class Internacao {
        -Long id
        -long versao
        -Long pacienteId
        -Long profissionalId
        -Long quartoId
        -LocalDate dataEntrada
        -LocalDate dataPrevistaAlta
        -LocalDate dataAltaEfetiva
        -String observacoes
        +atualizar()
        +registrarAlta(LocalDate)
        +ativa() boolean
    }
    class HistoricoMedico {
        <<record>>
        +PacienteResumo paciente
        +List atendimentos
        +List internacoes
    }
    Atendimento <|-- Consulta
    Consulta <|-- ConsultaRotina
    Consulta <|-- ConsultaEspecializada
    Atendimento <|-- Emergencia
    Atendimento <|-- Cirurgia
    Atendimento --> SituacaoAtendimento
    Emergencia --> Prioridade
    ProfissionalSaude --> CategoriaProfissional
    Quarto --> TipoQuarto
    Paciente "1" -- "0..*" Atendimento : pacienteId
    ProfissionalSaude "1" -- "0..*" Atendimento : profissionalId
    Paciente "1" -- "0..*" Internacao : pacienteId
    ProfissionalSaude "1" -- "0..*" Internacao : profissionalId
    Quarto "1" -- "0..*" Internacao : quartoId
    HistoricoMedico ..> Paciente : resumo
    HistoricoMedico ..> Atendimento : respostas
    HistoricoMedico ..> Internacao : respostas
```

## Camadas e colaboração

Cada módulo contém Controller, Service, Repository, Model e DTOs. Controllers
validam a entrada; services coordenam transações e contratos; modelos protegem
comportamentos; repositories acessam o banco. Classes de infraestrutura foram
separadas do diagrama de domínio para facilitar a leitura.

- `PacienteService` implementa `ConsultaPacientes`; `ProfissionalService` implementa
  `ConsultaProfissionais`. A ponte `Cadastros` valida referências entre módulos.
- `AtendimentoService` implementa `ConsultaDisponibilidade`; profissionais a usam
  para filtrar disponibilidade, sem criar outra agenda.
- `AgendaRepository` mantém a reserva única por profissional e início e usa o
  bloqueio transacional de `controle_agenda`. Herança dos atendimentos usa JPA
  `SINGLE_TABLE` com discriminador de modalidade.
- `QuartoService` usa `ConsultaOcupacaoQuartos`, implementada por
  `OcupacaoInternacoes`. Entrada, alta e alteração de capacidade bloqueiam o mesmo
  quarto. A multiplicidade inclui todas as internações históricas; a capacidade
  limita somente as ativas.
- `HistoricoService` agrega os DTOs dos services de atendimentos e internações.
  `HistoricoMedico` é uma visão calculada, sem tabela duplicada de prontuário.
- `SituacaoQuarto` (DISPONIVEL/OCUPADO) e `SituacaoInternacao` (ATIVA/ENCERRADA)
  são calculadas nas respostas. Situação ocupada não significa ausência de vaga.

A tabela de transições e as decisões sobre tipos estão na
[entrega consolidada](EntregaSprint2.md).
