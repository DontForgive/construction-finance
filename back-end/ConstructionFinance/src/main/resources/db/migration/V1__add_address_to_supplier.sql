-- HU-1.1: endereço do fornecedor (colunas nulas, sem valor padrão)
ALTER TABLE supplier ADD COLUMN street varchar(255);
ALTER TABLE supplier ADD COLUMN number varchar(20);
ALTER TABLE supplier ADD COLUMN neighborhood varchar(100);
ALTER TABLE supplier ADD COLUMN city varchar(100);
ALTER TABLE supplier ADD COLUMN state varchar(2);
ALTER TABLE supplier ADD COLUMN zip_code varchar(9);
ALTER TABLE supplier ADD COLUMN complement varchar(100);
