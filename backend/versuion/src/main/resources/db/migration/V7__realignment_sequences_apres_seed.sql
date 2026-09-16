-- ============================================================================
-- V7 : Réalignement de hibernate_sequence après le seed NOVATRA (ids 900+)
-- ============================================================================
-- Le seed (V5) insère des lignes avec des identifiants fixes ≥ 900, mais la
-- séquence Hibernate reste à sa valeur d'origine. Sans réalignement, la
-- prochaine entité créée par l'application recevrait un id déjà utilisé
-- (erreur de clé primaire en cascade).
-- On positionne la séquence au-delà du plus grand id de toutes les tables.
-- Idempotent : setval est déterministe.
-- ============================================================================

SELECT setval(
    'hibernate_sequence',
    GREATEST(
        (SELECT COALESCE(MAX(id), 0) FROM entreprise),
        (SELECT COALESCE(MAX(id), 0) FROM utilisateurs),
        (SELECT COALESCE(MAX(id), 0) FROM roles),
        (SELECT COALESCE(MAX(id), 0) FROM category),
        (SELECT COALESCE(MAX(id), 0) FROM article),
        (SELECT COALESCE(MAX(id), 0) FROM client),
        (SELECT COALESCE(MAX(id), 0) FROM fournisseur),
        (SELECT COALESCE(MAX(id), 0) FROM commande_client),
        (SELECT COALESCE(MAX(id), 0) FROM ligne_comande_client),
        (SELECT COALESCE(MAX(id), 0) FROM comande_fournisseur),
        (SELECT COALESCE(MAX(id), 0) FROM ligne_comande_fournisseur),
        (SELECT COALESCE(MAX(id), 0) FROM ventes),
        (SELECT COALESCE(MAX(id), 0) FROM ligne_vente),
        (SELECT COALESCE(MAX(id), 0) FROM mvt_stk)
    ) + 1,
    false
);
