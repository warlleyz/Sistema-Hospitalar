# Sistema Hospitalar

Trabalho prático da disciplina de Programação Modular do curso de Engenharia de Software da PUC Minas.

## Objetivo

Organizar os dados de pacientes, profissionais da saúde, consultas, internações, quartos e histórico médico de um hospital.

## Sprint 1

O projeto está na Sprint 1, dedicada às telas estáticas e à modelagem inicial. As entregas são:

- [Front-end estático](frontend/index.html), com as telas do sistema;
- [Diagrama de classes](docs/diagrama-classes.svg), também disponível em [Mermaid](docs/diagrama-classes.md);
- [Cartões CRC](docs/cartoes-crc.docx), com as seis classes do domínio.

As telas usam dados de exemplo e ainda não possuem funcionalidade. Os botões não cadastram, agendam, consultam ou salvam informações.

## Como visualizar

Baixe o repositório e abra `frontend/index.html` no navegador. O menu permite navegar entre as telas. Não é necessário instalar dependências ou iniciar um servidor.

## Preparação para a Sprint 2

A base foi organizada para iniciar a próxima etapa. O cadastro visual de quartos,
a indicação de vagas e a navegação acessível complementam o protótipo estático.
Não há backend, API, banco de dados ou testes de regras de negócio implementados.

- [Estado e limitações da Sprint 1](docs/estado-atual.md).
- [Plano, responsáveis e critérios da Sprint 2](docs/sprint-2/plano.md).
- [Contratos propostos e decisões pendentes](docs/sprint-2/contratos.md).
- [Fluxo de contribuição e revisão](CONTRIBUTING.md).

O primeiro passo de implementação será integrar a fundação Spring Boot e os
contratos comuns. As branches independentes de funcionalidades devem partir
desse marco. As decisões propostas na documentação ainda precisam ser validadas
na fundação; não são endpoints ou classes já existentes.

## Organização

```text
frontend/             Telas HTML e CSS da Sprint 1
docs/                 Diagrama, cartões CRC e estado da base
docs/sprint-2/        Planejamento e contratos da próxima etapa
.github/              Modelo para revisão de pull requests
```

Os PDFs do enunciado não estão versionados. O plano distingue exigências dos
documentos fornecidos, decisões técnicas propostas e dúvidas para o professor.

## Integrantes

- Warlley Silva Baião Braga
- Mateus Andrade Motta
- Luiz Gustavo Moura e Souza
