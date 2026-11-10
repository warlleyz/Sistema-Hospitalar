-- Preserva a precisao de LocalDateTime aceita pela API e consultada por JDBC.
-- Nao modifica os checksums das migracoes que ja foram aplicadas.
ALTER TABLE atendimento ALTER COLUMN inicio_previsto TIMESTAMP(9) NOT NULL;
ALTER TABLE atendimento ALTER COLUMN inicio_efetivo TIMESTAMP(9);
ALTER TABLE atendimento ALTER COLUMN fim_efetivo TIMESTAMP(9);
ALTER TABLE reserva_agenda ALTER COLUMN inicio_previsto TIMESTAMP(9) NOT NULL;
