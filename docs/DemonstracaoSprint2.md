# Demonstração da Sprint 2

Execute a partir de `backend/` com JDK 25. Em Git Bash:

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

No PowerShell, use `.\mvnw.cmd` no lugar de `./mvnw`.
Abra outro terminal Git Bash para os comandos HTTP abaixo. Os IDs 1 são
ilustrativos: substitua pelos IDs recebidos. Use CPF, registro e número de quarto
novos ao repetir a demonstração; duplicidades corretamente retornam 409.

## 1. Cadastrar paciente e clínico

```bash
curl -i -X POST http://localhost:8080/pacientes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Paciente Demonstracao","cpf":"12345678909","dataNascimento":"1990-01-01","telefone":"31999990000","endereco":"Rua A","email":"paciente@example.org"}'

curl -i -X POST http://localhost:8080/profissionais \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Clinico Demonstracao","registroProfissional":"CRM-DEMO-1","categoria":"CLINICO_GERAL","especialidade":"Clinica Geral","telefone":"31999990000","email":"medico@example.org"}'
```

Esperado: 201 e `Location`, com IDs reais. Não há mais necessidade de cadastros
simulados na aplicação integrada. Especialistas usam categoria ESPECIALISTA e sua
especialidade; cirurgiões usam CIRURGIAO e devem também informar `areaCirurgica`.

## 2. Agendar, validar conflito e registrar consulta

```bash
curl -i -X POST http://localhost:8080/consultas \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"ROTINA","pacienteId":1,"profissionalId":1,"inicioPrevisto":"2026-11-20T10:00:00","motivo":"Avaliacao"}'
```

Esperado: 201. Repetir o mesmo agendamento deve retornar 409.
Com o ID retornado:

```bash
curl -i -X PATCH http://localhost:8080/consultas/1/situacao \
  -H 'Content-Type: application/json' \
  -d '{"situacao":"EM_ATENDIMENTO","momento":"2026-11-20T10:00:00"}'

curl -i -X PATCH http://localhost:8080/consultas/1/registro \
  -H 'Content-Type: application/json' \
  -d '{"observacoes":"Orientacoes registradas","encaminhamento":"Cardiologia"}'

curl -i -X PATCH http://localhost:8080/consultas/1/situacao \
  -H 'Content-Type: application/json' \
  -d '{"situacao":"CONCLUIDA","momento":"2026-11-20T10:30:00"}'
```

Esperado: 200. Tentar voltar para AGENDADA deve retornar 409.

## 3. Quarto, internação e alta

```bash
curl -i -X POST http://localhost:8080/quartos \
  -H 'Content-Type: application/json' \
  -d '{"numero":"DEMO-101","andar":1,"capacidadeMaxima":1,"tipo":"INDIVIDUAL"}'

curl -i -X POST http://localhost:8080/internacoes \
  -H 'Content-Type: application/json' \
  -d '{"pacienteId":1,"profissionalId":1,"quartoId":1,"dataEntrada":"2026-11-20","dataPrevistaAlta":"2026-11-23","observacoes":"Acompanhamento"}'

curl -i http://localhost:8080/quartos/1
```

Esperado: entradas 201; quarto OCUPADO, ocupação 1 e vagas 0. Nova internação no
mesmo quarto deve retornar 409. Alta prevista não libera vaga.

```bash
curl -i -X PATCH http://localhost:8080/internacoes/1/alta \
  -H 'Content-Type: application/json' \
  -d '{"dataAltaEfetiva":"2026-11-22"}'

curl -i http://localhost:8080/quartos/1
curl -i http://localhost:8080/pacientes/1/historico
```

Esperado: alta 200; quarto com uma vaga. Histórico contém consulta CONCLUIDA com
observações e internação ENCERRADA. Repetir a alta retorna 409.

## 4. Emergência e cirurgia

```bash
curl -i -X POST http://localhost:8080/emergencias \
  -H 'Content-Type: application/json' \
  -d '{"pacienteId":1,"profissionalId":1,"entrada":"2026-11-20T11:00:00","motivo":"Avaliacao imediata","prioridade":"ALTA"}'

curl -i 'http://localhost:8080/emergencias?situacao=AGUARDANDO'
```

Esperado: 201 e listagem 200 com prioridade ALTA. Use o ID recebido para iniciar e
concluir, pelas mesmas rotas `/situacao` da consulta.

Após cadastrar um profissional CIRURGIAO com `areaCirurgica`, substitua o ID:

```bash
curl -i -X POST http://localhost:8080/cirurgias \
  -H 'Content-Type: application/json' \
  -d '{"pacienteId":1,"profissionalId":2,"inicioPrevisto":"2026-11-20T12:00:00","descricao":"Procedimento demonstrativo","tipoCirurgia":"Geral","observacoesPreOperatorias":"Avaliacao previa"}'
```

Esperado: 201 com cirurgião real; usar clínico retorna 409. Tentar concluir sem
iniciar retorna 409. Para registrar pós-operatório, conclua após início e envie
`observacoes` e `observacoesPosOperatorias` em `/cirurgias/{id}/registro`.

## 5. Validações rápidas

- Paciente inexistente: `GET /pacientes/999999` retorna 404.
- Cadastro `{}`: POST retorna 400 com campos inválidos.
- Tipo de quarto INDIVIDUAL com capacidade 2: retorna 409.
- `"prioridade":3`, `"tipo":0` ou `"situacao":5`: retorna 400; use nomes dos enums.
- Consulta especializada com especialidade incompatível: retorna 409.
- `GET /pacientes/{id}/historico` de outro paciente não inclui os registros acima.

Para encerrar, pressione `Ctrl+C` no terminal do servidor. Os dados normais ficam
no banco em arquivo; não o apague para atualizar, pois as migrações são automáticas.
