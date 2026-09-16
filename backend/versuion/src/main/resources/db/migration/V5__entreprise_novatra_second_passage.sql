-- ============================================================================
-- V5 : NOVATRA DISTRIBUTION SA — création du schéma si absent + données
-- ============================================================================
-- Comportement selon l'état de la base :
--   * Base existante (tables déjà créées par Hibernate) :
--       - CREATE TABLE IF NOT EXISTS / CREATE SEQUENCE : sans effet ;
--       - si l'entreprise 900 existe déjà (insérée par V4) : rien ne se passe.
--   * Base fraîche : Flyway s'exécute AVANT Hibernate (ddl-auto=update).
--       V4 est donc ressorti sans rien insérer. V5 crée alors lui-même les
--       14 tables métier (DDL identique à celui d'Hibernate) et insère les
--       données. Hibernate complète ensuite le schéma (tables restantes).
-- ============================================================================
-- 🔑 Comptes créés (mot de passe pour tous : « Novatra@2026 ») :
--   ADMIN   : achille.mballa@novatra-distribution.cm
--   MANAGER : clarisse.ndongo@novatra-distribution.cm
--   VENDEUR : emmanuel.fotso@novatra-distribution.cm
--   VENDEUR : sandrine.tchoumi@novatra-distribution.cm
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 0. Schéma minimal compatible Hibernate (noms de tables/colonnes identiques)
-- ---------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1000 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS entreprise (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    ville              varchar(255),
    addresse1          varchar(255),
    addresse2          varchar(255),
    code_postale       varchar(255),
    pays               varchar(255),
    codefiscale        varchar(255),
    description        varchar(255),
    email              varchar(255),
    nom                varchar(255),
    num_tel            varchar(255),
    photo              varchar(255),
    site_web           varchar(255)
);

CREATE TABLE IF NOT EXISTS utilisateurs (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    ville              varchar(255),
    addresse1          varchar(255),
    addresse2          varchar(255),
    code_postale       varchar(255),
    pays               varchar(255),
    date_de_naissance  varchar(255),
    email              varchar(255),
    mot_de_passe       varchar(255),
    nom                varchar(255),
    photo              varchar(255),
    prenom             varchar(255),
    identreprise       bigint
);

CREATE TABLE IF NOT EXISTS roles (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    rolename           varchar(255),
    id_utilisateur     bigint
);

CREATE TABLE IF NOT EXISTS category (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    code_category      varchar(255),
    designation        varchar(255),
    identreprise       integer
);

CREATE TABLE IF NOT EXISTS article (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    code_article       varchar(255),
    designation        varchar(255),
    identreprise       integer,
    photo              varchar(255),
    prix_unitaire      numeric(38,2),
    prix_unitairettc   numeric(38,2),
    taux_tva           numeric(38,2),
    id_category        bigint,
    seuil_alerte       numeric(38,2)
);

CREATE TABLE IF NOT EXISTS client (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    ville              varchar(255),
    addresse1          varchar(255),
    addresse2          varchar(255),
    code_postale       varchar(255),
    pays               varchar(255),
    identreprise       integer,
    mail               varchar(255),
    nom                varchar(255),
    num_tel            varchar(255),
    photo              varchar(255),
    prenom             varchar(255)
);

CREATE TABLE IF NOT EXISTS fournisseur (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    ville              varchar(255),
    addresse1          varchar(255),
    addresse2          varchar(255),
    code_postale       varchar(255),
    pays               varchar(255),
    identreprise       integer,
    mail               varchar(255),
    nom                varchar(255),
    num_tel            varchar(255),
    photo              varchar(255),
    prenom             varchar(255)
);

CREATE TABLE IF NOT EXISTS commande_client (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    code               varchar(255),
    date_comande       timestamp,
    etatcommande       varchar(255),
    identreprise       integer,
    id_client          bigint
);

CREATE TABLE IF NOT EXISTS ligne_comande_client (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    identreprise       integer,
    prix_unitaire      numeric(38,2),
    quantite           numeric(38,2),
    id_article         bigint,
    id_commande_client bigint
);

CREATE TABLE IF NOT EXISTS comande_fournisseur (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    code               varchar(255),
    date_comande       timestamp,
    etatcommande       varchar(255),
    identreprise       integer,
    idfournisseur      bigint
);

CREATE TABLE IF NOT EXISTS ligne_comande_fournisseur (
    id                    bigint PRIMARY KEY,
    creation_date         timestamp NOT NULL,
    last_modified_date    timestamp,
    identreprise          integer,
    prix_unitaire         numeric(38,2),
    quantite              numeric(38,2),
    id_article            bigint,
    idcommandefournisseur bigint
);

CREATE TABLE IF NOT EXISTS ventes (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    code               varchar(255),
    commentaire        varchar(255),
    date_vente         timestamp,
    identreprise       integer
);

CREATE TABLE IF NOT EXISTS ligne_vente (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    identreprise       integer,
    prix_unitare       numeric(38,2),
    quantie            numeric(38,2),
    idarticle          bigint,
    id_vent            bigint
);

CREATE TABLE IF NOT EXISTS mvt_stk (
    id                 bigint PRIMARY KEY,
    creation_date      timestamp NOT NULL,
    last_modified_date timestamp,
    date_mvt           timestamp,
    identreprise       integer,
    quantite           numeric(38,2),
    sourcemvt          varchar(255),
    type_mvt           varchar(255),
    idearticle         bigint
);

-- ---------------------------------------------------------------------------
-- 1. Données NOVATRA DISTRIBUTION SA (idempotent : garde sur l'entreprise 900)
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    v_entreprise   integer := 900;
    v_admin        bigint;
    v_manager      bigint;
    v_vendeur      bigint;
    v_vendeur2     bigint;
    v_cat_elec     bigint;
    v_cat_info     bigint;
    v_cat_alim     bigint;
    v_cat_hyg      bigint;
    v_cat_maison   bigint;
    v_client1      bigint;
    v_client2      bigint;
    v_client3      bigint;
    v_client4      bigint;
    v_client5      bigint;
    v_frs1         bigint;
    v_frs2         bigint;
    v_frs3         bigint;
    v_art_ciment   bigint;
    v_art_fer      bigint;
    v_art_pc       bigint;
    v_art_imp      bigint;
    v_art_riz      bigint;
    v_art_huile    bigint;
    v_art_savon    bigint;
    v_art_matelas  bigint;
    v_cmd1         bigint;
    v_cmd2         bigint;
    v_cmd3         bigint;
    v_cmdf1        bigint;
    v_cmdf2        bigint;
    v_vente1       bigint;
    v_vente2       bigint;
    v_vente3       bigint;
    v_vente4       bigint;
    v_vente5       bigint;
    v_vente6       bigint;
BEGIN
    IF EXISTS (SELECT 1 FROM entreprise WHERE id = v_entreprise) THEN
        RAISE NOTICE 'V5: entreprise NOVATRA déjà présente, rien à faire.';
        RETURN;
    END IF;

    ----------------------------------------------------------------------------
    -- Entreprise + comptes du personnel (bcrypt de « Novatra@2026 »)
    ----------------------------------------------------------------------------
    INSERT INTO entreprise (id, creation_date, nom, description,
                            addresse1, addresse2, ville, code_postale, pays,
                            codefiscale, email, num_tel, site_web)
    VALUES (v_entreprise, now(),
            'NOVATRA DISTRIBUTION SA',
            'Négoce de matériaux de construction, électronique et produits de grande consommation',
            'Boulevard du 20 Mai, Immeuble Zagaba', 'Face Ministère des Finances',
            'Yaoundé', 'BP 6122', 'Cameroun',
            'M012360123456N', 'contact@novatra-distribution.cm', '+237 6 99 45 12 78',
            'https://www.novatra-distribution.cm');

    INSERT INTO utilisateurs (id, creation_date, nom, prenom, email, date_de_naissance,
                              mot_de_passe, addresse1, ville, code_postale, pays, identreprise)
    VALUES (901, now(), 'Mballa', 'Achille', 'achille.mballa@novatra-distribution.cm', '1982-03-14',
            '$2a$10$ZwJxlDl0HkDYDSmKpOEbwedvwIL.iCpjOG4ebDye55lXQJryFwXwe',
            'Quartier Bastos', 'Yaoundé', 'BP 6122', 'Cameroun', v_entreprise);
    SELECT id INTO v_admin FROM utilisateurs WHERE email = 'achille.mballa@novatra-distribution.cm';

    INSERT INTO utilisateurs (id, creation_date, nom, prenom, email, date_de_naissance,
                              mot_de_passe, addresse1, ville, code_postale, pays, identreprise)
    VALUES (902, now(), 'Ndongo', 'Clarisse', 'clarisse.ndongo@novatra-distribution.cm', '1988-09-02',
            '$2a$10$o73/qBgvPwyD6ByS3SFwGukeF2Ihec7DS8nK2iOzX5XwYMExd1spS',
            'Quartier Nlongkak', 'Yaoundé', 'BP 6122', 'Cameroun', v_entreprise);
    SELECT id INTO v_manager FROM utilisateurs WHERE email = 'clarisse.ndongo@novatra-distribution.cm';

    INSERT INTO utilisateurs (id, creation_date, nom, prenom, email, date_de_naissance,
                              mot_de_passe, addresse1, ville, code_postale, pays, identreprise)
    VALUES (903, now(), 'Fotso', 'Emmanuel', 'emmanuel.fotso@novatra-distribution.cm', '1993-11-21',
            '$2a$10$ZwJxlDl0HkDYDSmKpOEbwedvwIL.iCpjOG4ebDye55lXQJryFwXwe',
            'Quartier Mvog-Ada', 'Yaoundé', 'BP 6122', 'Cameroun', v_entreprise);
    SELECT id INTO v_vendeur FROM utilisateurs WHERE email = 'emmanuel.fotso@novatra-distribution.cm';

    INSERT INTO utilisateurs (id, creation_date, nom, prenom, email, date_de_naissance,
                              mot_de_passe, addresse1, ville, code_postale, pays, identreprise)
    VALUES (904, now(), 'Tchoumi', 'Sandrine', 'sandrine.tchoumi@novatra-distribution.cm', '1995-06-08',
            '$2a$10$o73/qBgvPwyD6ByS3SFwGukeF2Ihec7DS8nK2iOzX5XwYMExd1spS',
            'Quartier Odza', 'Yaoundé', 'BP 6122', 'Cameroun', v_entreprise);
    SELECT id INTO v_vendeur2 FROM utilisateurs WHERE email = 'sandrine.tchoumi@novatra-distribution.cm';

    INSERT INTO roles (id, creation_date, rolename, id_utilisateur) VALUES (901, now(), 'ADMIN', v_admin);
    INSERT INTO roles (id, creation_date, rolename, id_utilisateur) VALUES (902, now(), 'MANAGER', v_manager);
    INSERT INTO roles (id, creation_date, rolename, id_utilisateur) VALUES (903, now(), 'VENDEUR', v_vendeur);
    INSERT INTO roles (id, creation_date, rolename, id_utilisateur) VALUES (904, now(), 'VENDEUR', v_vendeur2);

    ----------------------------------------------------------------------------
    -- Catégories
    ----------------------------------------------------------------------------
    INSERT INTO category (id, creation_date, code_category, designation, identreprise) VALUES (901, now(), 'MAT-CONSTR', 'Matériaux de construction', v_entreprise);
    INSERT INTO category (id, creation_date, code_category, designation, identreprise) VALUES (902, now(), 'ELECTRO', 'Électronique & électroménager', v_entreprise);
    INSERT INTO category (id, creation_date, code_category, designation, identreprise) VALUES (903, now(), 'ALIM-GEN', 'Alimentation générale', v_entreprise);
    INSERT INTO category (id, creation_date, code_category, designation, identreprise) VALUES (904, now(), 'HYG-CLEAN', 'Hygiène & entretien', v_entreprise);
    INSERT INTO category (id, creation_date, code_category, designation, identreprise) VALUES (905, now(), 'MAISON', 'Maison & literie', v_entreprise);
    SELECT id INTO v_cat_elec   FROM category WHERE code_category = 'MAT-CONSTR' AND identreprise = v_entreprise;
    SELECT id INTO v_cat_info   FROM category WHERE code_category = 'ELECTRO'    AND identreprise = v_entreprise;
    SELECT id INTO v_cat_alim   FROM category WHERE code_category = 'ALIM-GEN'   AND identreprise = v_entreprise;
    SELECT id INTO v_cat_hyg    FROM category WHERE code_category = 'HYG-CLEAN'  AND identreprise = v_entreprise;
    SELECT id INTO v_cat_maison FROM category WHERE code_category = 'MAISON'     AND identreprise = v_entreprise;

    ----------------------------------------------------------------------------
    -- Articles (prix en FCFA HT/TTC, TVA 19,25 %)
    ----------------------------------------------------------------------------
    INSERT INTO article (id, creation_date, code_article, designation, identreprise,
                         prix_unitaire, prix_unitairettc, taux_tva, id_category, seuil_alerte)
    VALUES (901, now(), 'ND-CIM-050', 'Ciment CIMAF 50 kg', v_entreprise, 4650, 5545, 19.25, v_cat_elec, 100);
    SELECT id INTO v_art_ciment FROM article WHERE code_article = 'ND-CIM-050' AND identreprise = v_entreprise;

    INSERT INTO article (id, creation_date, code_article, designation, identreprise,
                         prix_unitaire, prix_unitairettc, taux_tva, id_category, seuil_alerte)
    VALUES (902, now(), 'ND-FER-008', 'Fer à béton HA8 - barre 12 m', v_entreprise, 3900, 4651, 19.25, v_cat_elec, 80);
    SELECT id INTO v_art_fer FROM article WHERE code_article = 'ND-FER-008' AND identreprise = v_entreprise;

    INSERT INTO article (id, creation_date, code_article, designation, identreprise,
                         prix_unitaire, prix_unitairettc, taux_tva, id_category, seuil_alerte)
    VALUES (903, now(), 'ND-ELC-115', 'Ordinateur portable HP 250 G9 - i5/8Go/512Go', v_entreprise, 385000, 459113, 19.25, v_cat_info, 5);
    SELECT id INTO v_art_pc FROM article WHERE code_article = 'ND-ELC-115' AND identreprise = v_entreprise;

    INSERT INTO article (id, creation_date, code_article, designation, identreprise,
                         prix_unitaire, prix_unitairettc, taux_tva, id_category, seuil_alerte)
    VALUES (904, now(), 'ND-ELC-220', 'Imprimante Canon PIXMA G3420', v_entreprise, 128500, 153240, 19.25, v_cat_info, 6);
    SELECT id INTO v_art_imp FROM article WHERE code_article = 'ND-ELC-220' AND identreprise = v_entreprise;

    INSERT INTO article (id, creation_date, code_article, designation, identreprise,
                         prix_unitaire, prix_unitairettc, taux_tva, id_category, seuil_alerte)
    VALUES (905, now(), 'ND-ALI-310', 'Riz parfumé Indica - sac 25 kg', v_entreprise, 16250, 19378, 19.25, v_cat_alim, 60);
    SELECT id INTO v_art_riz FROM article WHERE code_article = 'ND-ALI-310' AND identreprise = v_entreprise;

    INSERT INTO article (id, creation_date, code_article, designation, identreprise,
                         prix_unitaire, prix_unitairettc, taux_tva, id_category, seuil_alerte)
    VALUES (906, now(), 'ND-ALI-355', 'Huile de palme raffinée - bidon 20 L', v_entreprise, 19500, 23251, 19.25, v_cat_alim, 30);
    SELECT id INTO v_art_huile FROM article WHERE code_article = 'ND-ALI-355' AND identreprise = v_entreprise;

    INSERT INTO article (id, creation_date, code_article, designation, identreprise,
                         prix_unitaire, prix_unitairettc, taux_tva, id_category, seuil_alerte)
    VALUES (907, now(), 'ND-HYG-410', 'Savon de ménage Mara - carton 20x400 g', v_entreprise, 8400, 10017, 19.25, v_cat_hyg, 25);
    SELECT id INTO v_art_savon FROM article WHERE code_article = 'ND-HYG-410' AND identreprise = v_entreprise;

    INSERT INTO article (id, creation_date, code_article, designation, identreprise,
                         prix_unitaire, prix_unitairettc, taux_tva, id_category, seuil_alerte)
    VALUES (908, now(), 'ND-MAI-505', 'Matelas mousse haute densité 140x190', v_entreprise, 62000, 73927, 19.25, v_cat_maison, 8);
    SELECT id INTO v_art_matelas FROM article WHERE code_article = 'ND-MAI-505' AND identreprise = v_entreprise;

    ----------------------------------------------------------------------------
    -- Clients
    ----------------------------------------------------------------------------
    INSERT INTO client (id, creation_date, nom, prenom, mail, num_tel,
                        addresse1, ville, code_postale, pays, identreprise)
    VALUES (901, now(), 'SARL BATIPRO CONSTRUCTION', 'Service achats',
            'achats@batipro-cm.com', '+237 6 77 12 34 56',
            'Rue Nachtigal, Zone industrielle', 'Yaoundé', 'BP 5401', 'Cameroun', v_entreprise);
    SELECT id INTO v_client1 FROM client WHERE mail = 'achats@batipro-cm.com' AND identreprise = v_entreprise;

    INSERT INTO client (id, creation_date, nom, prenom, mail, num_tel,
                        addresse1, ville, code_postale, pays, identreprise)
    VALUES (902, now(), 'ETS LA GRACE ENTREPOTS', 'Michel Nkodo',
            'michel.nkodo@lagrace.cm', '+237 6 55 87 21 09',
            'Marché Mokolo, Allée 4', 'Yaoundé', 'BP 3187', 'Cameroun', v_entreprise);
    SELECT id INTO v_client2 FROM client WHERE mail = 'michel.nkodo@lagrace.cm' AND identreprise = v_entreprise;

    INSERT INTO client (id, creation_date, nom, prenom, mail, num_tel,
                        addresse1, ville, code_postale, pays, identreprise)
    VALUES (903, now(), 'GROUPE SCOLAIRE LES PALMIERS', 'Intendance',
            'intendance@lespalmiers.cm', '+237 6 91 45 78 23',
            'Route de Sangmélima, Mvan', 'Yaoundé', 'BP 8210', 'Cameroun', v_entreprise);
    SELECT id INTO v_client3 FROM client WHERE mail = 'intendance@lespalmiers.cm' AND identreprise = v_entreprise;

    INSERT INTO client (id, creation_date, nom, prenom, mail, num_tel,
                        addresse1, ville, code_postale, pays, identreprise)
    VALUES (904, now(), 'PHARMACIE DU CENTRE', 'Dr Estelle Ngono',
            'estelle.ngono@pharmacie-centre.cm', '+237 6 78 34 56 90',
            'Avenue Kennedy, Immeuble Relance', 'Yaoundé', 'BP 1123', 'Cameroun', v_entreprise);
    SELECT id INTO v_client4 FROM client WHERE mail = 'estelle.ngono@pharmacie-centre.cm' AND identreprise = v_entreprise;

    INSERT INTO client (id, creation_date, nom, prenom, mail, num_tel,
                        addresse1, ville, code_postale, pays, identreprise)
    VALUES (905, now(), 'SOCIETE AGRO-SEMENCES SA', 'Directeur logistique',
            'logistique@agro-semences.cm', '+237 6 96 12 45 67',
            'Zone industrielle de Bassa', 'Douala', 'BP 9183', 'Cameroun', v_entreprise);
    SELECT id INTO v_client5 FROM client WHERE mail = 'logistique@agro-semences.cm' AND identreprise = v_entreprise;

    ----------------------------------------------------------------------------
    -- Fournisseurs
    ----------------------------------------------------------------------------
    INSERT INTO fournisseur (id, creation_date, nom, prenom, mail, num_tel,
                             addresse1, ville, code_postale, pays, identreprise)
    VALUES (901, now(), 'CIMAF CAMEROUN SA', 'Représentant commercial',
            'ventes@cimaf-cameroun.cm', '+237 2 33 42 18 60',
            'Zone industrielle de Bassa', 'Douala', 'BP 4021', 'Cameroun', v_entreprise);
    SELECT id INTO v_frs1 FROM fournisseur WHERE mail = 'ventes@cimaf-cameroun.cm' AND identreprise = v_entreprise;

    INSERT INTO fournisseur (id, creation_date, nom, prenom, mail, num_tel,
                             addresse1, ville, code_postale, pays, identreprise)
    VALUES (902, now(), 'IMPORT TECH DOUALA SARL', 'Service export',
            'export@importtech-dla.cm', '+237 2 33 50 77 41',
            'Rue de la Joie, Bonabéri', 'Douala', 'BP 6654', 'Cameroun', v_entreprise);
    SELECT id INTO v_frs2 FROM fournisseur WHERE mail = 'export@importtech-dla.cm' AND identreprise = v_entreprise;

    INSERT INTO fournisseur (id, creation_date, nom, prenom, mail, num_tel,
                             addresse1, ville, code_postale, pays, identreprise)
    VALUES (903, now(), 'COMPTOIR AGRO-BASSA', 'Département gros',
            'gros@agro-bassa.cm', '+237 2 33 41 22 08',
            'Rue du Roi Bell, Bassa', 'Douala', 'BP 7312', 'Cameroun', v_entreprise);
    SELECT id INTO v_frs3 FROM fournisseur WHERE mail = 'gros@agro-bassa.cm' AND identreprise = v_entreprise;

    ----------------------------------------------------------------------------
    -- Commandes clients + lignes
    ----------------------------------------------------------------------------
    INSERT INTO commande_client (id, creation_date, code, date_comande, etatcommande, identreprise, id_client)
    VALUES (901, now(), 'CMD-2026-0141', now() - interval '75 days', 'LIVREE', v_entreprise, v_client1);
    SELECT id INTO v_cmd1 FROM commande_client WHERE code = 'CMD-2026-0141' AND identreprise = v_entreprise;

    INSERT INTO commande_client (id, creation_date, code, date_comande, etatcommande, identreprise, id_client)
    VALUES (902, now(), 'CMD-2026-0156', now() - interval '40 days', 'LIVREE', v_entreprise, v_client2);
    SELECT id INTO v_cmd2 FROM commande_client WHERE code = 'CMD-2026-0156' AND identreprise = v_entreprise;

    INSERT INTO commande_client (id, creation_date, code, date_comande, etatcommande, identreprise, id_client)
    VALUES (903, now(), 'CMD-2026-0172', now() - interval '6 days', 'EN_PREPARATION', v_entreprise, v_client3);
    SELECT id INTO v_cmd3 FROM commande_client WHERE code = 'CMD-2026-0172' AND identreprise = v_entreprise;

    INSERT INTO ligne_comande_client (id, creation_date, identreprise, id_article, id_commande_client, quantite, prix_unitaire)
    VALUES (921, now(), v_entreprise, v_art_ciment, v_cmd1, 200, 4650);
    INSERT INTO ligne_comande_client (id, creation_date, identreprise, id_article, id_commande_client, quantite, prix_unitaire)
    VALUES (922, now(), v_entreprise, v_art_fer, v_cmd1, 120, 3900);
    INSERT INTO ligne_comande_client (id, creation_date, identreprise, id_article, id_commande_client, quantite, prix_unitaire)
    VALUES (923, now(), v_entreprise, v_art_riz, v_cmd2, 40, 16250);
    INSERT INTO ligne_comande_client (id, creation_date, identreprise, id_article, id_commande_client, quantite, prix_unitaire)
    VALUES (924, now(), v_entreprise, v_art_huile, v_cmd2, 25, 19500);
    INSERT INTO ligne_comande_client (id, creation_date, identreprise, id_article, id_commande_client, quantite, prix_unitaire)
    VALUES (925, now(), v_entreprise, v_art_pc, v_cmd3, 6, 385000);
    INSERT INTO ligne_comande_client (id, creation_date, identreprise, id_article, id_commande_client, quantite, prix_unitaire)
    VALUES (926, now(), v_entreprise, v_art_imp, v_cmd3, 4, 128500);

    ----------------------------------------------------------------------------
    -- Commandes fournisseurs + lignes
    ----------------------------------------------------------------------------
    INSERT INTO comande_fournisseur (id, creation_date, code, date_comande, etatcommande, identreprise, idfournisseur)
    VALUES (911, now(), 'CMD-F-2026-0031', now() - interval '60 days', 'LIVREE', v_entreprise, v_frs1);
    SELECT id INTO v_cmdf1 FROM comande_fournisseur WHERE code = 'CMD-F-2026-0031' AND identreprise = v_entreprise;

    INSERT INTO comande_fournisseur (id, creation_date, code, date_comande, etatcommande, identreprise, idfournisseur)
    VALUES (912, now(), 'CMD-F-2026-0044', now() - interval '12 days', 'VALIDEE', v_entreprise, v_frs2);
    SELECT id INTO v_cmdf2 FROM comande_fournisseur WHERE code = 'CMD-F-2026-0044' AND identreprise = v_entreprise;

    INSERT INTO ligne_comande_fournisseur (id, creation_date, identreprise, id_article, idcommandefournisseur, quantite, prix_unitaire)
    VALUES (931, now(), v_entreprise, v_art_ciment, v_cmdf1, 600, 4650);
    INSERT INTO ligne_comande_fournisseur (id, creation_date, identreprise, id_article, idcommandefournisseur, quantite, prix_unitaire)
    VALUES (932, now(), v_entreprise, v_art_fer, v_cmdf1, 300, 3900);
    INSERT INTO ligne_comande_fournisseur (id, creation_date, identreprise, id_article, idcommandefournisseur, quantite, prix_unitaire)
    VALUES (933, now(), v_entreprise, v_art_pc, v_cmdf2, 15, 385000);
    INSERT INTO ligne_comande_fournisseur (id, creation_date, identreprise, id_article, idcommandefournisseur, quantite, prix_unitaire)
    VALUES (934, now(), v_entreprise, v_art_imp, v_cmdf2, 12, 128500);

    ----------------------------------------------------------------------------
    -- Ventes + lignes (réparties sur 5 mois + mois courant)
    ----------------------------------------------------------------------------
    INSERT INTO ventes (id, creation_date, code, date_vente, commentaire, identreprise)
    VALUES (951, now(), 'V-2026-0101', now() - interval '130 days', 'Chantier résidentiel Bastos - phase 1', v_entreprise);
    SELECT id INTO v_vente1 FROM ventes WHERE code = 'V-2026-0101' AND identreprise = v_entreprise;

    INSERT INTO ventes (id, creation_date, code, date_vente, commentaire, identreprise)
    VALUES (952, now(), 'V-2026-0118', now() - interval '100 days', 'Réassort grossiste Mokolo', v_entreprise);
    SELECT id INTO v_vente2 FROM ventes WHERE code = 'V-2026-0118' AND identreprise = v_entreprise;

    INSERT INTO ventes (id, creation_date, code, date_vente, commentaire, identreprise)
    VALUES (953, now(), 'V-2026-0134', now() - interval '75 days', 'Dotation informatique - rentrée scolaire', v_entreprise);
    SELECT id INTO v_vente3 FROM ventes WHERE code = 'V-2026-0134' AND identreprise = v_entreprise;

    INSERT INTO ventes (id, creation_date, code, date_vente, commentaire, identreprise)
    VALUES (954, now(), 'V-2026-0149', now() - interval '45 days', 'Marché public - pharmacie de proximité', v_entreprise);
    SELECT id INTO v_vente4 FROM ventes WHERE code = 'V-2026-0149' AND identreprise = v_entreprise;

    INSERT INTO ventes (id, creation_date, code, date_vente, commentaire, identreprise)
    VALUES (955, now(), 'V-2026-0163', now() - interval '20 days', 'Livraison Douala - Agro-Semences', v_entreprise);
    SELECT id INTO v_vente5 FROM ventes WHERE code = 'V-2026-0163' AND identreprise = v_entreprise;

    INSERT INTO ventes (id, creation_date, code, date_vente, commentaire, identreprise)
    VALUES (956, now(), 'V-2026-0171', now() - interval '2 days', 'Vente comptoir', v_entreprise);
    SELECT id INTO v_vente6 FROM ventes WHERE code = 'V-2026-0171' AND identreprise = v_entreprise;

    INSERT INTO ligne_vente (id, creation_date, identreprise, idarticle, id_vent, quantie, prix_unitare)
    VALUES (961, now(), v_entreprise, v_art_ciment, v_vente1, 120, 4650);
    INSERT INTO ligne_vente (id, creation_date, identreprise, idarticle, id_vent, quantie, prix_unitare)
    VALUES (962, now(), v_entreprise, v_art_fer, v_vente1, 60, 3900);
    INSERT INTO ligne_vente (id, creation_date, identreprise, idarticle, id_vent, quantie, prix_unitare)
    VALUES (963, now(), v_entreprise, v_art_riz, v_vente2, 55, 16250);
    INSERT INTO ligne_vente (id, creation_date, identreprise, idarticle, id_vent, quantie, prix_unitare)
    VALUES (964, now(), v_entreprise, v_art_huile, v_vente2, 30, 19500);
    INSERT INTO ligne_vente (id, creation_date, identreprise, idarticle, id_vent, quantie, prix_unitare)
    VALUES (965, now(), v_entreprise, v_art_pc, v_vente3, 4, 385000);
    INSERT INTO ligne_vente (id, creation_date, identreprise, idarticle, id_vent, quantie, prix_unitare)
    VALUES (966, now(), v_entreprise, v_art_savon, v_vente4, 45, 8400);
    INSERT INTO ligne_vente (id, creation_date, identreprise, idarticle, id_vent, quantie, prix_unitare)
    VALUES (967, now(), v_entreprise, v_art_matelas, v_vente5, 7, 62000);
    INSERT INTO ligne_vente (id, creation_date, identreprise, idarticle, id_vent, quantie, prix_unitare)
    VALUES (968, now(), v_entreprise, v_art_imp, v_vente6, 2, 128500);

    ----------------------------------------------------------------------------
    -- Mouvements de stock : entrées fournisseurs + sorties ventes.
    -- Le riz (55 < 60) et le savon (20 < 25) sont volontairement sous leur
    -- seuil d'alerte pour alimenter le tableau de bord.
    ----------------------------------------------------------------------------
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (971, now(), now() - interval '60 days',  v_entreprise, 500, 'ENTREE', 'COMMANDE_FOURNISSEUR', v_art_ciment);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (972, now(), now() - interval '60 days',  v_entreprise, 250, 'ENTREE', 'COMMANDE_FOURNISSEUR', v_art_fer);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (973, now(), now() - interval '12 days',  v_entreprise, 12,  'ENTREE', 'COMMANDE_FOURNISSEUR', v_art_pc);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (974, now(), now() - interval '12 days',  v_entreprise, 10,  'ENTREE', 'COMMANDE_FOURNISSEUR', v_art_imp);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (975, now(), now() - interval '110 days', v_entreprise, 90,  'ENTREE', 'COMMANDE_FOURNISSEUR', v_art_riz);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (976, now(), now() - interval '110 days', v_entreprise, 55,  'ENTREE', 'COMMANDE_FOURNISSEUR', v_art_huile);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (977, now(), now() - interval '70 days',  v_entreprise, 65,  'ENTREE', 'COMMANDE_FOURNISSEUR', v_art_savon);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (978, now(), now() - interval '50 days',  v_entreprise, 20,  'ENTREE', 'COMMANDE_FOURNISSEUR', v_art_matelas);

    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (981, now(), now() - interval '130 days', v_entreprise, 120, 'SORTIE', 'VENTE', v_art_ciment);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (982, now(), now() - interval '130 days', v_entreprise, 60,  'SORTIE', 'VENTE', v_art_fer);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (983, now(), now() - interval '100 days', v_entreprise, 55,  'SORTIE', 'VENTE', v_art_riz);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (984, now(), now() - interval '100 days', v_entreprise, 30,  'SORTIE', 'VENTE', v_art_huile);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (985, now(), now() - interval '75 days',  v_entreprise, 4,   'SORTIE', 'VENTE', v_art_pc);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (986, now(), now() - interval '45 days',  v_entreprise, 45,  'SORTIE', 'VENTE', v_art_savon);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (987, now(), now() - interval '20 days',  v_entreprise, 7,   'SORTIE', 'VENTE', v_art_matelas);
    INSERT INTO mvt_stk (id, creation_date, date_mvt, identreprise, quantite, type_mvt, sourcemvt, idearticle) VALUES (988, now(), now() - interval '2 days',   v_entreprise, 2,   'SORTIE', 'VENTE', v_art_imp);

    RAISE NOTICE 'V5: entreprise NOVATRA DISTRIBUTION SA créée avec succès.';
END $$;
