# Fundação técnica da Sprint 2

## Tecnologias

- Java 25 (LTS), compatível com o JDK 25 disponível nesta máquina.
- Spring Boot 3.5.16, Spring MVC, Bean Validation e Spring Data JPA.
- Maven Wrapper 3.9.16: não exige Maven instalado, mas exige JDK e internet no primeiro uso.
- H2 relacional em arquivo, com transações e restrições; Flyway cria o esquema e
  Hibernate apenas valida. Não usar `ddl-auto=update` nem modificar migração aplicada.
- Testes usam H2 em memória isolado do arquivo local.

H2 foi escolhido por permitir persistência relacional real, sem servidor externo,
facilitando a reprodução do trabalho pelos três integrantes. É adequado à entrega
acadêmica local; não representa escolha de infraestrutura para um hospital real.
Os dados locais ficam em `backend/data/`, ignorados no Git. Execute a partir de
`backend/` para manter o caminho relativo consistente. Não habilitar console H2.

Referência de compatibilidade: https://docs.spring.io/spring-boot/3.5/system-requirements.html

## Execução

No PowerShell, a partir de `backend/`:

```powershell
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run
```

No Linux/macOS, usar `./mvnw`. A API usa `http://localhost:8080`.
`DB_URL`, `DB_USER` e `DB_PASSWORD` podem sobrescrever a conexão. Não versionar segredos.
As telas HTML continuam estáticas e não consomem a API nesta entrega.

## Organização e contratos implementados

Pacote raiz: `br.edu.pucminas.hospital`.
As frentes usam `pacientes`, `profissionais`, `atendimentos`, `quartos` e `internacoes`,
com camadas internas `model`, `repository`, `service`, `controller` e `dto`.

Contratos públicos em `compartilhado.contratos`:

```java
interface ConsultaPacientes {
    Optional<PacienteResumo> buscarPorId(Long id);
}
record PacienteResumo(Long id, String nome) {}
interface ConsultaProfissionais {
    Optional<ProfissionalResumo> buscarPorId(Long id);
}
record ProfissionalResumo(Long id, String nome,
    CategoriaProfissional categoria, String especialidade) {}
```

`CategoriaProfissional`: `CLINICO_GERAL`, `ESPECIALISTA`, `CIRURGIAO`.
Mateus registra exatamente um bean para cada interface; ausência de registro retorna
`Optional.empty()`. Não lançar erro de infraestrutura para representar ID inexistente.
A ponte `Cadastros` transforma ausência em 404 e módulo não integrado em 503.
Não há implementação fictícia de cadastro em produção. Luiz pode reutilizar a ponte.
IDs são `Long` positivos. Referências entre módulos são IDs, sem associação JPA com
entidades alheias; validar usando os contratos e não oferecer exclusão de cadastros
referenciados. A integração final deve verificar a integridade desses vínculos.

Cada módulo fornece sua própria migração Flyway, com versões por frente para não
colidir: Warlley `V100__...`, Mateus `V200__...`, Luiz `V300__...`; incrementos dentro
de cada faixa. `spring.flyway.out-of-order=true` aceita integração das faixas fora de
ordem, pois as tabelas dos módulos não dependem de ordem entre frentes.
Não criar chaves estrangeiras para tabelas de outra frente antes de combinar.

## Convenções fechadas para esta implementação

- Datas/horários locais do hospital (`America/Sao_Paulo`), sem conversão implícita.
- Cirurgia usa `LocalDateTime` para início previsto e efetivo e término efetivo.
- Sem duração especificada pelo PDF: a agenda impede igualdade de início do mesmo
  profissional entre modalidades. Sobreposição de intervalos não é implementada.
- Consultas agendadas, confirmadas e em atendimento reservam horário. Cancelamento
  e não comparecimento liberam; concluídas preservam a reserva histórica.
- Cirurgia agendada/em andamento/concluída reserva; cancelada libera.
- Emergência usa o horário de entrada como reserva enquanto aguarda ou está ativa,
  sem ultrapassar uma reserva existente; concluída preserva e cancelada libera.
  Não se implementa fila automática ou preempção de outro atendimento.
- Validação específica é feita também antes de iniciar o atendimento.
- Especialidades são comparadas ignorando caixa e espaços externos.
- Encaminhamento: especialidade de destino registrada em consulta de rotina,
  sem agendar automaticamente uma segunda consulta.
- Dados clínicos são registrados em atendimento ou na conclusão; não aceitar
  observações pós-atendimento preenchidas como se a consulta já tivesse ocorrido.

Essas são decisões técnicas de implementação do grupo, não requisitos adicionais
atribuídos ao professor. CPF sem pontuação, registro profissional com normalização
consistente e número de quarto único são políticas de integridade recomendadas
para as frentes responsáveis documentarem e aplicarem.

## Pendências que não bloqueiam atendimentos

Tipos de quartos continuam sem definição no enunciado: Luiz deve registrar a dúvida
para o professor e não inventar categorias obrigatórias. O ciclo mínimo proposto de
internação é ativa (alta efetiva ausente) e encerrada (alta efetiva presente), com
previsão/alta não anteriores à entrada; sem transferência ou reabertura implícita.
Luiz deve validar/documentar esse ciclo na sua frente.

## Erros comuns

Corpo: `codigo`, `mensagem`, `campos` (lista de `campo` e `mensagem`).
400: formato/validação; 404: registro inexistente; 409: regra, estado ou concorrência;
503: dependência de cadastro ainda não integrada. Não expor stack trace.

## Contrato da disponibilidade

`ConsultaDisponibilidade.disponivel(Long profissionalId, LocalDateTime inicio)` é
implementado por `AtendimentoService`. Mateus pode injetar essa interface para
combinar especialidade e horário na busca de profissionais. O resultado é uma
consulta informativa, não uma reserva: o registro do atendimento revalida o horário
sob bloqueio no banco. A chamada pode retornar 404 para profissional inexistente
ou 503 enquanto os cadastros não estiverem integrados.
