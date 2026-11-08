# Entrega de Warlley: fundação e atendimentos

Branch: `warlley`. Implementação limitada à fundação compartilhada e à frente de
atendimentos. Não implementa as tarefas de Mateus ou Luiz, nem conclui a Sprint 2
inteira. As branches finais dos colegas são `mateus` e `luiz`.

## Implementado

- Spring Boot, Maven Wrapper, JPA, H2 persistente em arquivo e migrações Flyway.
- Contratos de consulta de pacientes/profissionais e formato comum de erros.
- Hierarquia com comportamento: Atendimento, Consulta, ConsultaRotina,
  ConsultaEspecializada, Emergencia e Cirurgia.
- API, persistência, validação de profissional e especialidade por modalidade.
- Estados controlados, registro clínico, encaminhamento e observações cirúrgicas.
- Agenda única entre modalidades com proteção transacional e restrição no banco.
- Consulta dos registros por paciente, modalidade e situação.
- Interface pública de disponibilidade para a frente de profissionais.

## Execução e testes

Requisitos e contratos completos estão na [fundação](sprint-2/fundacao.md).
No PowerShell, a partir de `backend/`:

```powershell
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run
```

Alternativamente, após o build:

```powershell
java -jar target/hospital-0.0.1-SNAPSHOT.jar
```

Foram executados 23 testes sem falhas:

- 10 de domínio: categorias, compatibilidade, estados, prioridades, datas,
  encaminhamento e informações clínicas.
- 10 de integração/API: fluxos, erros, isolamento por paciente, rollback,
  disponibilidade e duas requisições concorrentes para o mesmo horário.
- 1 que fecha e reabre contextos da aplicação sobre o mesmo banco em arquivo e
  verifica a preservação do atendimento e da reserva.
- 2 de fundação: inicialização sem cadastros e resposta 503 explícita na API.

O build limpo `clean verify` também gerou o JAR executável. Em teste manual do JAR
na porta local 18081, `GET /consultas` retornou `200 []` e o cadastro sem o módulo
de pacientes retornou `503 MODULO_INDISPONIVEL`. O servidor de verificação foi
encerrado depois dessas consultas.

Os dados de pacientes/profissionais dos testes existem somente em
`src/test/java/.../CadastrosTestConfig.java`; não são seed ou cadastro em produção.
Os testes são executados com JDK 25 e compilam para Java 25.

## Recursos HTTP

| Método | Caminho | Operação |
| --- | --- | --- |
| POST | `/consultas` | Criar rotina ou especializada |
| POST | `/emergencias` | Registrar entrada com prioridade |
| POST | `/cirurgias` | Agendar procedimento |
| GET | `/consultas`, `/emergencias`, `/cirurgias` | Listar a modalidade |
| GET | `/{recurso}/{id}` | Consultar registro da modalidade |
| PATCH | `/{recurso}/{id}/situacao` | Executar transição válida |
| PATCH | `/{recurso}/{id}/registro` | Registrar dados clínicos |
| GET | `/atendimentos/disponibilidade?profissionalId=10&inicio=2026-11-20T10:00:00` | Consultar o horário |

`recurso` aceita somente `consultas`, `emergencias` ou `cirurgias`.
Listagens aceitam `pacienteId` e `situacao`, opcionalmente, e são ordenadas por
início e ID. O ID de uma cirurgia não pode ser consultado como se fosse consulta.
Listas não têm paginação nesta entrega.

Não há PUT genérico, exclusão ou alteração livre de estado. Para cancelar e
agendar novamente, preserve o registro cancelado e crie um novo atendimento.

### Exemplo: consulta de rotina

Requer paciente 1 e profissional 10 (clínico geral) reais, cadastrados pela frente
de Mateus. Sem esse módulo integrado, o resultado correto é 503, não 201.

```http
POST /consultas
Content-Type: application/json

{
  "tipo": "ROTINA",
  "pacienteId": 1,
  "profissionalId": 10,
  "inicioPrevisto": "2026-11-20T10:00:00",
  "motivo": "Avaliação de rotina"
}
```

Após integração e com horário livre: `201 Created`, `Location: /consultas/{id}`
e corpo semelhante a:

```json
{
  "id": 1,
  "modalidade": "ROTINA",
  "pacienteId": 1,
  "profissionalId": 10,
  "inicioPrevisto": "2026-11-20T10:00:00",
  "motivo": "Avaliação de rotina",
  "situacao": "AGENDADA"
}
```

Especializada usa `tipo: "ESPECIALIZADA"` e `especialidade: "Cardiologia"`, com
profissional da categoria ESPECIALISTA e especialidade compatível. Rotina exige
CLINICO_GERAL e não aceita especialidade solicitada na criação.

### Exemplo: iniciar, registrar e concluir

```http
PATCH /consultas/1/situacao
Content-Type: application/json

{"situacao":"EM_ATENDIMENTO","momento":"2026-11-20T10:03:00"}
```

```http
PATCH /consultas/1/registro
Content-Type: application/json

{"observacoes":"Orientações registradas","encaminhamento":"Cardiologia"}
```

```http
PATCH /consultas/1/situacao
Content-Type: application/json

{"situacao":"CONCLUIDA","momento":"2026-11-20T10:30:00"}
```

`momento` é obrigatório para início e conclusão efetivos. Encaminhamento é uma
especialidade de destino, sem agendamento automático. Observações exigem início
do atendimento; podem ser complementadas depois da conclusão.

### Exemplo: emergência

```http
POST /emergencias
Content-Type: application/json

{
  "pacienteId":1,
  "profissionalId":10,
  "entrada":"2026-11-20T11:00:00",
  "motivo":"Avaliação imediata",
  "prioridade":"ALTA"
}
```

Prioridades: BAIXA, MODERADA, ALTA, CRITICA. O registro inicia em AGUARDANDO.
`GET /emergencias?situacao=AGUARDANDO` permite identificar a espera.
Não há triagem ou despacho automático por prioridade. Início não pode anteceder
entrada. Profissional deve existir e pertencer a uma das categorias do contrato.

### Exemplo: cirurgia

```http
POST /cirurgias
Content-Type: application/json

{
  "pacienteId":1,
  "profissionalId":30,
  "inicioPrevisto":"2026-11-20T12:00:00",
  "descricao":"Descrição do procedimento",
  "tipoCirurgia":"Tipo registrado pelo hospital",
  "observacoesPreOperatorias":"Avaliação pré-operatória"
}
```

Exige categoria CIRURGIAO. Na resposta, `motivo` contém a descrição comum ao
atendimento. Início e término efetivos aparecem em `inicioEfetivo` e `fimEfetivo`.
Usar as transições EM_ATENDIMENTO e CONCLUIDA. Após concluir:

```http
PATCH /cirurgias/1/registro
Content-Type: application/json

{"observacoes":"Procedimento concluído","observacoesPosOperatorias":"Acompanhamento pós-operatório"}
```

O ID do exemplo deve ser substituído pelo ID retornado na criação da cirurgia.
Pré-operatório é opcional na criação; pós-operatório exige cirurgia concluída.

## Transições implementadas

| Modalidade | Origem | Destinos permitidos |
| --- | --- | --- |
| Consulta | AGENDADA | CONFIRMADA, EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU |
| Consulta | CONFIRMADA | EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU |
| Consulta | EM_ATENDIMENTO | CONCLUIDA |
| Emergência | AGUARDANDO | EM_ATENDIMENTO, CANCELADA |
| Emergência | EM_ATENDIMENTO | CONCLUIDA |
| Cirurgia | AGENDADA | EM_ATENDIMENTO, CANCELADA |
| Cirurgia | EM_ATENDIMENTO | CONCLUIDA |

Estados finais não têm saída. A cirurgia usa os mesmos literais do enum comum:
AGENDADA corresponde a agendado, EM_ATENDIMENTO a em andamento, CONCLUIDA a concluído
e CANCELADA a cancelado. Trata-se da padronização da API, mantendo os significados
do PDF. A confirmação da consulta é opcional antes do início por decisão do grupo.

Conclusão exige início anterior ou igual ao término. Ao iniciar, a categoria e a
especialidade do profissional são revalidadas contra o cadastro atual.

## Agenda e consistência

O PDF não define duração. A implementação impede o mesmo profissional no mesmo
instante previsto, entre todas as modalidades. Não calcula sobreposição de
intervalos e não libera reservas históricas de atendimentos concluídos.
Cancelamento e não comparecimento liberam o horário; um novo agendamento cria
outro registro. Profissionais diferentes podem usar o mesmo horário.

Toda escrita passa pela mesma transação e pelo bloqueio da linha em
`controle_agenda` (`SELECT FOR UPDATE`). A tabela `reserva_agenda` ainda impõe
unicidade por profissional e início. Criar atendimento e reservar, ou cancelar e
liberar, são operações atômicas. Registro clínico inválido sofre rollback completo.
A consulta de disponibilidade é informativa: a criação sempre revalida a reserva.

O bloqueio global simplifica a consistência, mas serializa escritas inclusive de
profissionais diferentes. É uma escolha para a escala acadêmica local. H2 em
arquivo é executado por uma instância da aplicação; não foi validada implantação
em múltiplos processos nem migração para outro banco.

## Integração dos colegas

Antes de criar suas branches, os colegas precisam partir da versão da `main` que
incorporar esta fundação após revisão. Esta entrega não executa merge ou push.

Mateus implementa um bean de `ConsultaPacientes` e um de `ConsultaProfissionais`,
com os tipos e assinaturas da [fundação](sprint-2/fundacao.md). Pode consumir
`ConsultaDisponibilidade` para combinar filtro de especialidade com horário.
Luiz consome os mesmos contratos ou a ponte `Cadastros`, sem duplicar entidades.
Cada frente mantém suas migrações na faixa documentada.

## Pendências fora desta frente

- Pacientes, profissionais e categorias persistidas: Mateus.
- Quartos, tipos de quartos, internações, capacidade e alta: Luiz.
- Tipos de quartos: esclarecer a lacuna com o professor; não definidos aqui.
- Histórico médico consolidado: integrar consultas/cirurgias/emergências com as
  internações após a entrega de Luiz. Esta frente já fornece seus registros por paciente.
- Diagrama global da Sprint 2: consolidar as três frentes. O
  [diagrama de atendimentos](sprint-2/diagrama-atendimentos.md) corresponde ao código atual.
- Integração das telas, autenticação e requisitos adicionais não foram implementados.

A Sprint 2 completa só pode ser declarada concluída depois da integração e dos
checks globais do plano. Os testes com interfaces simuladas não comprovam a
implementação dos módulos dos colegas.
