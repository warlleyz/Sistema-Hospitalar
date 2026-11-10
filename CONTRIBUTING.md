# Como contribuir

Use o README para executar a aplicação e `docs/EntregaSprint2.md` para consultar
as regras e decisões do projeto. O diagrama está em `docs/diagrama-classes.md`.

Trabalhe em uma branch própria e descreva no PR o comportamento alterado e os testes
executados. Preserve a autoria e o histórico dos integrantes. Alterações de contrato
entre módulos devem ser combinadas com os responsáveis.

Execute `mvnw clean verify` em `backend/` antes da integração. Não edite migrações
já aplicadas: adicione novas. Não versione banco local, credenciais ou arquivos de
build. As telas em `frontend/` permanecem protótipos estáticos.
