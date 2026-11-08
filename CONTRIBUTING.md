# Como contribuir

## Estado da base

As telas em `frontend/` sao prototipos estaticos. Nao existe backend neste marco.
Consulte [o plano da Sprint 2](docs/sprint-2/plano.md) antes de implementar.
O diagrama e os CRC existentes descrevem a Sprint 1; nao representam as novas
categorias e modalidades da Sprint 2.

## Branches e autoria

1. Warlley prepara e integra o PR de fundacao descrito no plano.
2. Depois desse merge, cada integrante atualiza a `main` e cria sua branch.
3. Cada um trabalha no seu modulo, incluindo regras, persistencia e testes.
4. Alteracoes em contratos ou arquivos compartilhados devem ser combinadas antes.
5. Abra um PR para a `main`; nao envie funcionalidades diretamente para ela.

Branches sugeridas:

- Warlley: `warlley`.
- Mateus: `mateus`.
- Luiz Gustavo: `luiz`.

Cada integrante deve usar sua propria identidade no Git e revisar o codigo que
entregar, inclusive quando houver assistencia de ferramentas. Faca commits por
etapa real e verificavel, sem fragmentacao artificial para aumentar contagens.
Prefira merge que preserve os commits e autores quando isso for compativel com as
regras do repositorio. Nao reescreva o historico compartilhado.

## Revisao

Use o [modelo de PR](.github/pull_request_template.md). Quem revisa deve conferir
regras, contratos e resultados dos testes, alem de conflitos textuais. Atualize a
branch com a `main` antes da revisao final e repita os testes afetados.

Para alterar o prototipo, abra as paginas afetadas no navegador e confira
navegacao, teclado, larguras estreitas, textos e coerencia dos exemplos. Mantenha
os botoes de negocio desabilitados enquanto nao houver implementacao real.

Nao versione credenciais, configuracoes pessoais, logs ou arquivos de build.
