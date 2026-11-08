# Contratos propostos para a fundacao

Estas sao propostas tecnicas do grupo, nao exigencias literais do professor nem
interfaces implementadas. Devem ser fechadas no PR de fundacao antes do trabalho
independente. Mudancas posteriores precisam ser combinadas entre os consumidores.

## Organizacao e dependencias

- Um backend Spring Boot em `backend/`, com um pacote raiz unico.
- Modulos `pacientes`, `profissionais`, `atendimentos`, `quartos` e `internacoes`.
- Cada modulo contem suas camadas Model, Repository, Service e Controller e DTOs.
- Controllers recebem DTOs; regras de dominio nao ficam em controllers.
- Compartilhar somente contratos necessarios, evitando dependencias circulares.
- Atendimentos e internacoes consultam pacientes e profissionais por interfaces
  publicas da fundacao. Nao acessam diretamente repositories de outro modulo.
- As interfaces retornam dados suficientes para validacao: ID, categoria e
  especialidade do profissional. Definir assinaturas e DTOs exatos na fundacao.
- Uma unica fonte controla a agenda; uma unica fonte calcula ocupacao ativa.

## Convencoes propostas

| Tema | Proposta |
| --- | --- |
| IDs | `Long`, positivos, gerados no backend |
| Referencias de entrada | `pacienteId`, `profissionalId`, `quartoId` |
| Datas e horarios | ISO 8601; datas locais `YYYY-MM-DD`, horarios `HH:mm:ss` |
| Fuso | Horarios locais do hospital; documentar `America/Sao_Paulo` |
| Situacoes e prioridades | Enums; sem texto livre ou alteracao arbitraria |
| Campos de alta efetiva | Opcionais enquanto a internacao estiver ativa |
| Historico | Consulta por paciente, sem copiar registros para fontes independentes |

Decidir no contrato se datas e horarios previstos/efetivos da cirurgia serao
campos separados ou pares representados por `LocalDateTime`. Usar a mesma escolha
nos DTOs, no modelo e nos exemplos.

## API proposta

O PDF exige os cinco recursos, mas nao especifica verbos ou formatos. Proposta:

- `POST /recurso`: criar, responder `201` com identificador e `Location`.
- `GET /recurso` e `GET /recurso/{id}`: listar e consultar, responder `200`.
- `PUT /recurso/{id}`: atualizar dados permitidos, responder `200`.
- Nao oferecer exclusao de registros referenciados ou historicos sem definir
  politica de integridade. CRUD completo nao foi explicitado no PDF.
- Transicoes de estado devem passar por operacoes de negocio especificas;
  definir as rotas e payloads antes de implementar. Nao aceitar qualquer estado
  pelo endpoint generico de atualizacao.
- Definir filtros por especialidade, paciente e periodo e as rotas de emergencia,
  cirurgia e historico no PR da respectiva frente.

Erros propostos: `400` para formato ou campos invalidos, `404` para recurso
inexistente e `409` para conflito de estado, agenda ou capacidade.
Corpo comum proposto: `codigo`, `mensagem`, `campos` (lista de erros de campo).
Nao retornar stack trace ou detalhes de infraestrutura ao cliente.

## Decisoes de negocio a fechar

1. **Tipos de quartos:** o item 4 das entregas cita tipos, sem descrever categorias
   ou regras. Consultar o professor; nao inventar uma exigencia academica.
2. **Internacoes:** o texto introdutorio cita suas situacoes, sem lista ou fluxo.
   Definir o ciclo de entrada/alta e o tratamento de datas invalidas.
3. **Agenda:** o requisito inicial proibe mesmo horario para um profissional, mas
   nao define duracao. Definir se conflito e igualdade de inicio ou sobreposicao,
   e quais estados reservam/liberam horario. Definir como emergencia interage
   com esse controle, sem presumir que pode causar dupla reserva.
4. **Estados:** publicar a tabela completa de transicoes permitidas de consulta,
   emergencia e cirurgia. Os exemplos do PDF nao definem todas as transicoes.
5. **Encaminhamento:** definir a representacao minima do encaminhamento do clinico.
6. **Integridade:** definir unicidade de CPF, registro profissional e numero do
   quarto, e normalizacao desses valores. Sao propostas de integridade a validar.
7. **Banco:** escolher relacional ou MongoDB e justificar tecnicamente, conforme
   o enunciado geral; nao usar memoria como substituto da persistencia final.
8. **Frontend:** confirmar se integracao nesta sprint e esperada pelo professor;
   nao aparece explicitamente entre as entregas listadas.

Testes de cada frente devem cobrir rejeicoes e transicoes, alem dos casos validos.
Testes de integracao devem conferir os contratos reais e a protecao contra
requisicoes simultaneas nos recursos disputados.
