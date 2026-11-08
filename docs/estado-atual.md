# Estado do protótipo da Sprint 1

Este documento preserva o diagnóstico do marco de preparação. O estado atual do
backend está no [README](../README.md) e na [entrega de Warlley](EntregaWarlley.md).

## Marco preservado

A entrega original da Sprint 1 termina no commit `84517f7`. A preparacao seguinte
complementa o prototipo e a documentacao, sem implementar a Sprint 2.

O repositorio contem sete paginas HTML, um CSS compartilhado, diagrama de classes
em Mermaid e SVG e seis cartoes CRC em DOCX. Os formularios e listagens representam
pacientes, profissionais, consultas, internacoes, quartos e historico medico.
O dashboard apresenta apenas exemplos, nao indicadores calculados.

## Cobertura e limitacoes

- Os campos minimos do enunciado inicial estao representados nas telas e no modelo.
- O cadastro de quartos agora tem representacao visual; nao salva informacoes.
- Menus permitem navegar entre as sete paginas. As acoes de negocio ficam
  desabilitadas e as informacoes exibidas sao ficticias.
- Um quarto ocupado ainda pode ter vagas. A tabela exibe vagas separadamente da
  situacao e o dashboard conta quartos com vaga, nao apenas quartos vazios.
- Nao existem edicao, exclusao, agendamento, busca real, alta ou historico dinamico.
- Nao existem Java, Spring Boot, API, banco, excecoes de negocio ou testes
  automatizados. Os avisos de disponibilidade nas telas nao executam validacoes.

## Modelagem

O diagrama existente e um modelo inicial de dados e associacoes, sem metodos.
As responsabilidades comportamentais estao nos CRC. Na Sprint 2, o diagrama deve
ser atualizado junto com o codigo, incluindo metodos, abstracoes e estados.
Nao confundir a ausencia dessas extensoes com funcionalidades ja entregues.

Cuidados para a evolucao:

- Todas as consultas e internacoes de um historico devem pertencer ao seu paciente.
- A multiplicidade `0..*` entre quarto e internacao inclui registros ao longo do
  tempo; a capacidade limita somente as internacoes ativas simultaneamente.
- A alta prevista nao libera vaga; a alta efetiva encerra a ocupacao.
- Evitar duas fontes independentes para ocupacao ou historico que possam divergir.

## Limite da avaliacao da Sprint 1

O enunciado geral fornecido descreve o sistema do semestre, mas nao apresenta um
checklist especifico da Sprint 1. A cobertura acima foi comparada com o escopo
declarado no README e os requisitos iniciais. Ela nao comprova a nota, a aprovacao
ou o conteudo efetivamente enviado ao professor.
