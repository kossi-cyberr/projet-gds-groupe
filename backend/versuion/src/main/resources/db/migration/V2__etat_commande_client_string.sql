-- Convertit la colonne etatcommande de la table commande_client
-- (stockée en ordinal par Hibernate) vers sa valeur textuelle.
-- Compatible avec :
--   - une base fraîche (la table n'existe pas encore, le bloc est ignoré ;
--     Hibernate crée ensuite le schéma avec l'enum en STRING),
--   - une base existante (conversion 0/1/2 -> EN_PREPARATION/VALIDEE/LIVREE).
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'commande_client' AND column_name = 'etatcommande'
    ) THEN
        ALTER TABLE commande_client
            ALTER COLUMN etatcommande TYPE varchar(32)
            USING CASE
                WHEN etatcommande IN ('0') THEN 'EN_PREPARATION'
                WHEN etatcommande IN ('1') THEN 'VALIDEE'
                WHEN etatcommande IN ('2') THEN 'LIVREE'
                ELSE etatcommande::text
            END;
    END IF;
END $$;
