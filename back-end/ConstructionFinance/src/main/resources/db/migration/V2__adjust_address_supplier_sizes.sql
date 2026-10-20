-- HU-1.2: ajusta tamanhos das colunas de endereço criadas pela V1 (HU-1.1)
-- para os limites aprovados na spec (D2). Não altera dados existentes além do tipo.
ALTER TABLE supplier ALTER COLUMN street TYPE varchar(150);
ALTER TABLE supplier ALTER COLUMN number TYPE varchar(10);
ALTER TABLE supplier ALTER COLUMN zip_code TYPE varchar(8);
