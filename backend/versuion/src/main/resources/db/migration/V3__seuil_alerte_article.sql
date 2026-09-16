-- Ajoute la colonne seuil_alerte sur la table article (seuil de déclenchement
-- des alertes de stock faible).
-- Compatible avec :
--   - une base fraîche (la table n'existe pas encore, le bloc est ignoré ;
--     Hibernate crée ensuite le schéma avec la colonne),
--   - une base existante (ajout de la colonne si absente).
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'article')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'article' AND column_name = 'seuil_alerte') THEN
        ALTER TABLE article ADD COLUMN seuil_alerte numeric;
    END IF;
END $$;
