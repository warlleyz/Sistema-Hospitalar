# Sistema Hospitalar

Trabalho prático de Programação Modular, Engenharia de Software, PUC Minas.
Sistema para pacientes, profissionais, consultas, internações, quartos e histórico.

## Estado atual

- **Sprint 1:** sete telas estáticas, diagrama inicial e cartões CRC.
- **Sprint 2 / Warlley:** fundação Spring Boot, modalidades de atendimento,
  consultas, emergências, cirurgias, estados e agenda persistida implementados.
- **Mateus:** cadastros de pacientes e profissionais ainda pendentes de integração.
- **Luiz:** quartos e internações ainda pendentes de integração.

O backend inicia sem os módulos dos colegas. Operações que dependem de seus
cadastros retornam `503 MODULO_INDISPONIVEL` até que as interfaces sejam
implementadas. Não existem cadastros fictícios em produção.

## Executar o backend

Requer JDK 25 (LTS). No PowerShell:

```powershell
cd backend
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run
```

No Linux/macOS: `./mvnw verify` e `./mvnw spring-boot:run`.
O primeiro uso requer internet para baixar Maven e dependências.
API em `http://localhost:8080`; banco H2 em arquivo `backend/data/hospital.mv.db`.
Não é necessário instalar Maven ou um servidor de banco separado.

Recursos implementados: `/consultas`, `/emergencias`, `/cirurgias` e
`/atendimentos/disponibilidade`. Listagens sem registros retornam listas vazias.
Exemplos e limites estão na [entrega de Warlley](docs/EntregaWarlley.md).

## Visualizar o protótipo

Abra [frontend/index.html](frontend/index.html) no navegador.
As telas usam dados fictícios e não estão conectadas à API; botões de negócio
continuam desabilitados. O formulário de quartos não salva informações.

## Documentação

- [Entrega de Warlley: API, estados, testes e integração](docs/EntregaWarlley.md).
- [Fundação: tecnologias e contratos para os colegas](docs/sprint-2/fundacao.md).
- [Plano e responsabilidades da Sprint 2](docs/sprint-2/plano.md).
- [Propostas iniciais e dúvidas do enunciado](docs/sprint-2/contratos.md).
- [Diagrama implementado dos atendimentos](docs/sprint-2/diagrama-atendimentos.md).
- [Estado do protótipo e limitações da Sprint 1](docs/estado-atual.md).
- [Diagrama inicial SVG](docs/diagrama-classes.svg) e [Mermaid](docs/diagrama-classes.md).
- [Cartões CRC da Sprint 1](docs/cartoes-crc.docx).
- [Contribuição e revisão](CONTRIBUTING.md).

Os PDFs do enunciado não estão versionados. A documentação distingue exigências,
decisões técnicas e dúvidas ainda abertas, especialmente tipos de quartos.

## Integrantes

- Warlley Silva Baião Braga: branch `warlley`.
- Mateus Andrade Motta: branch `mateus`.
- Luiz Gustavo Moura e Souza: branch `luiz`.
