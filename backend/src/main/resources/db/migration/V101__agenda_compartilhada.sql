CREATE TABLE controle_agenda (id BIGINT PRIMARY KEY);
INSERT INTO controle_agenda(id) VALUES (1);
CREATE TABLE reserva_agenda (
    atendimento_id BIGINT PRIMARY KEY REFERENCES atendimento(id),
    profissional_id BIGINT NOT NULL,
    inicio_previsto TIMESTAMP NOT NULL,
    CONSTRAINT uk_reserva_profissional_inicio UNIQUE(profissional_id, inicio_previsto)
);
