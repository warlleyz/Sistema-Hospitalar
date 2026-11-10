ALTER TABLE quarto ADD COLUMN tipo VARCHAR(20);
UPDATE quarto SET tipo = CASE WHEN capacidade_maxima = 1 THEN 'INDIVIDUAL' ELSE 'COLETIVO' END;
ALTER TABLE quarto ALTER COLUMN tipo SET NOT NULL;
ALTER TABLE quarto ADD CONSTRAINT ck_quarto_tipo_capacidade CHECK (
    (tipo = 'INDIVIDUAL' AND capacidade_maxima = 1)
    OR (tipo = 'COLETIVO' AND capacidade_maxima >= 2)
);
