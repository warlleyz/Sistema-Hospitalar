# Sprint 2 — entrega consolidada

## Resultado e escopo

Integração das frentes de Warlley, Mateus e Luiz na `main`, com os commits originais
preservados e um único commit novo de integração/correções. API Spring Boot com
as camadas Controller, Service, Repository e Model, DTOs, exceções, persistência
e testes. As telas da Sprint 1 permanecem estáticas.

Requisitos de referência: `PM_TRABALHOPRATICO-3.pdf` e `SPRINT2_TPPM-2.pdf`, fornecidos
pelo grupo. Não estão versionados. Decisões de implementação não são apresentadas
como exigências literais dos documentos.

## Cobertura da entrega

| Requisito | Implementação |
| --- | --- |
| Pacientes | Campos mínimos, CPF normalizado/único, cadastro, consulta e atualização |
| Profissionais | Dados comuns, categorias, especialidade, área cirúrgica e disponibilidade |
| Modalidades | Rotina, especializada, emergência e cirurgia com validações próprias |
| Estados das consultas | Enum e transições controladas, sem reabertura de estado final |
| Emergência | Prioridade controlada, espera, início e conclusão |
| Cirurgia | Cirurgião, descrição, tipo, pré/pós-operatório e horários previstos/efetivos |
| Quartos | Número único, andar, tipo, capacidade, situação, ocupação e vagas |
| Internações | Paciente, responsável, quarto, datas, observações e alta efetiva |
| Disponibilidade | Reserva única por profissional e início previsto, com transação e bloqueio |
| Capacidade | Entrada, alta e redução de capacidade usam o mesmo bloqueio do quarto |
| Histórico | Consulta conjunta por paciente, preservando atendimentos e internações |
| Persistência | H2 relacional em arquivo, Flyway e validação do esquema pelo Hibernate |
| Diagrama | Modelo consolidado em `docs/diagrama-classes.md` |

Endpoints obrigatórios presentes: `/pacientes`, `/profissionais`, `/consultas`,
`/internacoes` e `/quartos`. Complementos: `/emergencias`, `/cirurgias`,
`/atendimentos/disponibilidade` e `/pacientes/{id}/historico`.

## Decisões e regras complementares

### Tipos de quartos

O enunciado cita tipos, mas não define categorias ou regras. O grupo adota uma
classificação simples por capacidade, sem presumir requisitos clínicos:

- `INDIVIDUAL`: capacidade exatamente 1.
- `COLETIVO`: capacidade de pelo menos 2.

`tipo` pode ser enviado no cadastro e atualização. Se omitido, é inferido da
capacidade para manter compatibilidade com os clientes da frente original. Uma
combinação explícita incompatível retorna 409, sem alteração parcial. A resposta
sempre informa o tipo. Para mudar tipo/capacidade, atualizar ambos de forma
compatível; continuam proibidas reduções abaixo da ocupação ativa.

A migração V302 classifica quartos existentes pela capacidade e aplica restrição
no banco. Essa escolha cobre a lacuna do enunciado como decisão documentada do
grupo; se o professor especificar categorias diferentes, o modelo deve ser ajustado.

### Estados

| Modalidade | Origem | Destinos |
| --- | --- | --- |
| Consulta | AGENDADA | CONFIRMADA, EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU |
| Consulta | CONFIRMADA | EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU |
| Consulta | EM_ATENDIMENTO | CONCLUIDA |
| Emergência | AGUARDANDO | EM_ATENDIMENTO, CANCELADA |
| Emergência | EM_ATENDIMENTO | CONCLUIDA |
| Cirurgia | AGENDADA | EM_ATENDIMENTO, CANCELADA |
| Cirurgia | EM_ATENDIMENTO | CONCLUIDA |
| Internação | ATIVA | ENCERRADA, pela alta efetiva |

Estados finais não têm saída. Confirmação é opcional antes de iniciar consulta.
Cirurgias usam os literais comuns: EM_ATENDIMENTO corresponde a em andamento.
Início e conclusão exigem `momento`; término não pode anteceder início. Observações
clínicas exigem início; pós-operatório exige conclusão da cirurgia. Categoria e
especialidade do profissional são revalidadas antes de iniciar.

Enums JSON exigem nomes, não posições numéricas. Erros: 400 para entrada inválida,
404 para inexistência, 409 para regra/conflito; 503 somente em cenários de módulos
não disponíveis. Os cadastros reais estão presentes na aplicação integrada.

### Agenda e precisão

Como não há duração definida no PDF, conflitos são por igualdade do início do
mesmo profissional entre modalidades, sem cálculo de sobreposição de intervalos.
Cancelamento/não comparecimento liberam a reserva; conclusão a preserva como
histórico. Emergência não preempta outro atendimento automaticamente.

API e banco suportam até nove casas decimais do segundo. V102 amplia as colunas
para `TIMESTAMP(9)`, evitando que o banco arredonde um horário que a consulta de
disponibilidade ainda considere livre. Valores antigos já arredondados permanecem
como estavam: a migração não pode recuperar precisão anteriormente perdida.

### Histórico consolidado

`GET /pacientes/{id}/historico` retorna:

- `paciente`: ID e nome.
- `atendimentos`: rotina, especializadas, emergências e cirurgias, ordenadas por início/ID.
- `internacoes`: registros ativos e encerrados, ordenados por entrada/ID.

Inclui situações de cada registro, permitindo distinguir agendados, realizados e
cancelados. Não duplica prontuários em outra tabela nem mistura pacientes. A leitura
conjunta usa uma transação com isolamento REPEATABLE_READ. Consulta inexistente
retorna 404; paciente existente sem registros retorna listas vazias.

### Internações

Alta prevista não libera vaga. Alta efetiva encerra a internação e libera sua vaga
na mesma transação, preservando observações e vínculos. Não há transferência,
reabertura ou exclusão de histórico. Datas são locais, sem impor validação contra o
relógio atual: a aplicação permite registro de dados de períodos diferentes, mas
previsão e alta nunca podem anteceder entrada. Não há agendamento futuro de leitos
por intervalo: ausência de alta é o critério de ocupação ativa.

## Banco e atualização

H2 em arquivo permite persistência relacional e reprodução local sem servidor
separado; a justificativa está na fundação. Não constitui plano de produção clínica.
Não editar migrações aplicadas nem apagar banco para atualizar. Flyway aceita
migrações fora de ordem para incorporar as faixas independentes das frentes.

- V100/V101: atendimentos e agenda.
- V102: precisão de horários.
- V200/V201: pacientes e profissionais.
- V300/V301: quartos e internações.
- V302: tipos de quartos e compatibilidade com capacidade.

## Validação executada

Com JDK 25, `mvnw clean verify`: **59 testes, sem falhas, erros ou ignorados**.
O build produz o JAR executável. Cobertura inclui domínio, APIs, dados inválidos,
compatibilidade de categorias, transições, rollback, unicidade, concorrência,
persistência após reinício e integração das três frentes com cadastros reais.

Os novos testes de entrega conferem histórico e isolamento por paciente, precisão
de nove casas, rejeição de enums numéricos e regras dos tipos. Um teste cria banco
no esquema anterior, insere dados e aplica as novas migrações, verificando que
quartos, atendimentos e reservas continuam presentes. Os testes de módulos ausentes
isolam essa condição explicitamente, sem conflitar com os cadastros reais.

## Limites declarados

- Frontend estático, sem consumo da API; a Sprint 2 solicitada é API/modelo.
- H2 local, uma instância da aplicação; agenda usa bloqueio global de escrita.
- Sem duração de atendimento, fila automática de emergência ou reserva futura de leitos.
- Listagens sem paginação. Sem autenticação ou auditoria clínica de produção.
- Tipos de quartos adotados pelo grupo, sujeitos a esclarecimento do professor.

## Material para apresentar

1. Executar os testes pelo wrapper.
2. Mostrar o [diagrama consolidado](diagrama-classes.md).
3. Seguir o [roteiro de demonstração](DemonstracaoSprint2.md).
4. Consultar entregas individuais para autoria e detalhes das frentes.
