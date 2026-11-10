# Sistema Hospitalar

Trabalho prático de Programação Modular, Engenharia de Software, PUC Minas.

## Sprint 2 — entrega integrada

API REST com pacientes, profissionais da saúde, consultas de rotina e especializadas,
emergências, cirurgias, quartos, internações, altas e histórico médico consolidado.
As três frentes estão reunidas na `main`, preservando os commits dos integrantes.

## Executar e testar

Requer **JDK 25**. O Maven Wrapper baixa Maven e dependências no primeiro uso;
não é necessário instalar Maven nem um servidor de banco separado.

No **Git Bash**, a partir da raiz:

```bash
cd backend
./mvnw clean verify
./mvnw spring-boot:run
```

No **PowerShell**, a partir da raiz:

```powershell
cd backend
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

O build integrado foi validado com **59 testes, sem falhas ou erros** e gera
`backend/target/hospital-0.0.1-SNAPSHOT.jar`. Para executar o JAR a partir de `backend/`:

```bash
java -jar target/hospital-0.0.1-SNAPSHOT.jar
```

API em `http://localhost:8080`. Encerre com `Ctrl+C`. O banco relacional H2 é
persistido em `backend/data/hospital.mv.db`, ignorado no Git. Os testes usam bancos
separados e não apagam dados da aplicação. O caminho do banco é relativo: inicie
sempre de `backend/`. Não há dados demonstrativos previamente cadastrados.

## Recursos

| Recurso | Finalidade |
| --- | --- |
| `/pacientes` | Cadastro, listagem, consulta e atualização |
| `/profissionais` | Cadastro, categorias e filtros por especialidade/horário |
| `/consultas` | Rotina, especializadas, estados e registros clínicos |
| `/emergencias` | Prioridade e evolução do atendimento |
| `/cirurgias` | Agendamento, execução e informações pré/pós-operatórias |
| `/quartos` | Capacidade, tipos, ocupação e vagas |
| `/internacoes` | Entrada, atualização, alta e histórico por paciente |
| `/pacientes/{id}/historico` | Histórico médico consolidado |
| `/atendimentos/disponibilidade` | Disponibilidade de profissional por início previsto |

Consulte o [roteiro de demonstração](docs/DemonstracaoSprint2.md) para testar os
fluxos com cadastros reais. Os corpos JSON e as regras estão nas entregas abaixo.

## Documentação de entrega

- [Entrega consolidada, decisões e critérios](docs/EntregaSprint2.md).
- [Diagrama de classes atualizado](docs/diagrama-classes.md).
- [Fundação e contratos entre módulos](docs/sprint-2/fundacao.md).
- [Frente de pacientes e profissionais](docs/EntregaMateus.md).
- [Frente de quartos e internações](docs/EntregaLuiz.md).
- [Frente de atendimentos](docs/EntregaWarlley.md).
- [Plano do grupo](docs/sprint-2/plano.md) e [contribuição](CONTRIBUTING.md).

## Integrantes e contribuições

| Integrante | Desenvolvimento | Contribuição |
| --- | --- | --- |
| Warlley Silva Baião Braga | `main` | Fundação, atendimentos, agenda, histórico consolidado e integração final |
| Mateus Andrade Motta | branch `mateus` | Pacientes, profissionais, categorias e busca por disponibilidade |
| Luiz Gustavo Moura e Souza | branch `luiz` | Quartos, internações, ocupação, alta e respectivos testes |

## Sprint 1 e limites do escopo

As sete telas em [frontend/](frontend/index.html) permanecem como protótipo estático,
com dados fictícios e ações desabilitadas; não estão conectadas à API. A Sprint 2
fornece o backend solicitado pelo enunciado. A entrega consolidada explicita as
decisões técnicas onde os PDFs não detalham as regras.

Preservados: [diagrama original](docs/sprint-1/diagrama-classes.svg),
[Mermaid original](docs/sprint-1/diagrama-classes.md), [cartões CRC](docs/cartoes-crc.docx)
e [diagnóstico da Sprint 1](docs/estado-atual.md).
