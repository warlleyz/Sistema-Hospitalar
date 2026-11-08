# Sprint 2: plano de implementacao

## Fontes e escopo

Fontes consultadas: `PM_TRABALHOPRATICO-3.pdf` (2 paginas) e
`SPRINT2_TPPM-2.pdf` (3 paginas), fornecidos pelo grupo e nao versionados.
Esta documentacao planeja a implementacao; nao declara funcionalidades prontas.

O enunciado da Sprint 2 exige:

- Diagrama atualizado e classes com atributos, metodos e regras de negocio.
- API Spring Boot com `/pacientes`, `/profissionais`, `/consultas`, `/internacoes`
  e `/quartos`.
- Clinico geral, especialista e cirurgiao com suas atribuicoes.
- Rotina, consulta especializada, emergencia e procedimento cirurgico, com
  compartilhamento de comportamento e validacao propria de cada modalidade.
- Prioridades de emergencia e acompanhamento de sua evolucao.
- Estados controlados de consulta e cirurgia e rejeicao de transicoes invalidas.
- Regras de categorias, modalidades, procedimentos e tipos de quartos.
- Continuidade das regras iniciais de disponibilidade, capacidade e historico.

O enunciado geral exige camadas Controller, Service, Repository e Model, banco
com justificativa tecnica, excecoes e testes automatizados ao longo do trabalho.
Recomenda-se incorpora-los a esta implementacao. A integracao das telas nao e
listada expressamente nas entregas da Sprint 2.

## Etapa 0: fundacao, antes das branches independentes

Responsavel pela integracao: Warlley. Este e o proximo trabalho, ainda pendente.

- [ ] Resolver ou registrar as duvidas de [contratos](contratos.md).
- [ ] Definir Java, Spring Boot e build compativeis e versionar o wrapper.
- [ ] Criar `backend/`, comando de inicializacao e verificacao de build.
- [ ] Escolher banco e justificar; definir configuracao local sem credenciais.
- [ ] Definir pacote raiz e organizacao por modulo com camadas internas.
- [ ] Publicar interfaces de consulta de pacientes e profissionais, incluindo os
      tipos de retorno, para as outras frentes poderem usar simulacoes nos testes.
- [ ] Definir formato comum de erro e estrategia de migracoes, se aplicavel.
- [ ] Integrar essa base na `main` e comunicar o commit de partida aos tres.

Nao criar classes de dominio vazias como se fossem implementacoes concluidas.
Nao iniciar tres projetos Spring Boot separados.

## Frentes

| Responsavel | Branch | Diretorios propostos dentro do pacote raiz |
| --- | --- | --- |
| Warlley | `codex/sprint2-atendimentos` | `atendimentos/` |
| Mateus | `codex/sprint2-cadastros` | `pacientes/`, `profissionais/` |
| Luiz Gustavo | `codex/sprint2-internacoes` | `quartos/`, `internacoes/` |

### Mateus: cadastros

- Pacientes com todos os dados minimos do enunciado.
- Profissionais com dados comuns e categorias de clinico, especialista e cirurgiao.
- Consulta de profissionais por especialidade; a disponibilidade de horario sera
  informada pelo modulo de atendimentos, sem criar uma segunda agenda.
- Endpoints de pacientes e profissionais, persistencia, validacoes e testes.
- Implementacoes reais das interfaces de consulta definidas na fundacao.
- Documentacao e trecho do diagrama em `docs/sprint-2/cadastros.md`.

### Warlley: atendimentos

- Dados e comportamento compartilhados e validacoes especificas das modalidades.
- Rotina com registro de observacoes e possibilidade de encaminhamento.
- Especializada com compatibilidade entre especialidade solicitada e profissional.
- Emergencia com prioridade e estados de espera, inicio e conclusao.
- Cirurgia com cirurgiao, tipo, descricao, observacoes pre e pos-operatorias,
  datas e horarios previstos e efetivos e estados controlados.
- Consultas com estados agendada, confirmada, em atendimento, concluida, cancelada
  e nao compareceu. Rejeitar transicoes incompativeis.
- Uma unica verificacao de conflitos para atendimentos que reservam horario.
- Endpoints de consultas e rotas adicionais documentadas para as modalidades.
- Persistencia, testes e documentacao em `docs/sprint-2/atendimentos.md`.

### Luiz Gustavo: quartos e internacoes

- Quartos com identificacao, andar, capacidade, situacao e vagas calculadas.
- Tipos de quartos somente apos esclarecer a lacuna do enunciado.
- Internacao vinculada a paciente, profissional e quarto, com datas e observacoes.
- Alta efetiva, liberacao da vaga e preservacao dos registros anteriores.
- Rejeicao de capacidade excedida, inclusive em requisicoes simultaneas.
- Consulta das internacoes de um paciente, endpoints, persistencia e testes.
- Documentacao em `docs/sprint-2/internacoes.md`.

Cada frente entrega suas camadas e testes; nao dividir a equipe por controller,
service e repository, pois isso tornaria toda funcionalidade dependente dos tres.

## Integracao e arquivos compartilhados

Warlley integra build, configuracao comum, erros globais e diagrama consolidado.
Cada integrante propoe mudancas compartilhadas no PR antes de depender delas.
Migracoes devem ter nomes exclusivos e ser conferidas juntas no merge.

As branches podem ser desenvolvidas em paralelo apos a fundacao. Ordem sugerida
de merge: cadastros, internacoes, atendimentos. As dependencias devem ser simuladas
nos testes enquanto as implementacoes reais nao estiverem disponiveis.

A consulta consolidada do historico fica como tarefa de integracao de Warlley,
usando registros fornecidos por cada modulo. Nao duplicar prontuarios. O historico
deve continuar consultavel apos alta ou conclusao.

## Criterios de conclusao

- [ ] Build e testes executam pelo comando documentado, a partir de checkout limpo.
- [ ] Os cinco caminhos exigidos possuem operacoes e exemplos documentados.
- [ ] Pacientes e profissionais inexistentes sao rejeitados nos vinculos.
- [ ] Conflitos de horario do mesmo profissional sao rejeitados.
- [ ] Consulta especializada exige profissional compativel.
- [ ] Emergencia informa prioridade e andamento.
- [ ] Cirurgia nao pode ser concluida sem inicio.
- [ ] Consulta concluida nao volta a agendada; cancelada nao e concluida diretamente.
- [ ] Quarto lotado rejeita entrada; alta efetiva libera vaga.
- [ ] Concorrencia nao permite ultrapassar capacidade ou duplicar reservas.
- [ ] Historico nao mistura pacientes e preserva atendimentos anteriores.
- [ ] Banco e decisoes tecnicas estao documentados; segredos nao estao versionados.
- [ ] Diagrama, codigo, instrucoes de execucao e exemplos da API concordam.

As caixas acima permanecem abertas ate haver evidencias de implementacao.
