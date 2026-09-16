-- ============================================================================
-- V6 : Correction du signe des mouvements de stock SORTIE (entreprise NOVATRA)
-- ============================================================================
-- L'application enregistre les sorties de stock avec une quantité NÉGATIVE
-- (cf. MvtStkServiceImpl#sortieNegative). Le seed V5 avait inséré les sorties
-- en positif, faussant le stock réel et les alertes de seuil.
-- On inverse le signe des SORTIE de l'entreprise 900 insérées en positif.
-- Idempotent : seules les quantités strictement positives sont inversées.
-- ============================================================================

UPDATE mvt_stk
SET quantite = -quantite
WHERE identreprise = 900
  AND type_mvt IN ('SORTIE', 'CORRECTION_NEG')
  AND quantite > 0;
