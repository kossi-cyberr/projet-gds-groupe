export interface TopArticle {
  idArticle: number;
  designation: string;
  codeArticle: string;
  quantiteVendue: number;
  chiffreAffaires: number;
}

export interface VentesParMois {
  annee: number;
  mois: number;
  chiffreAffaires: number;
}

export interface VentesParCategorie {
  idCategory: number;
  designation: string;
  chiffreAffaires: number;
}

export interface CommandesParClient {
  idClient: number;
  nom: string;
  prenom: string;
  nombreCommandes: number;
  montantTotal: number;
}

export interface Dashboard {
  chiffreAffairesTotal: number;
  chiffreAffairesMoisEnCours: number;
  chiffreAffairesJourEnCours: number;
  nombreClients: number;
  nombreFournisseurs: number;
  nombreArticles: number;
  nombreCommandesClient: number;
  nombreCommandesFournisseur: number;
  nombreVentes: number;
  stockSousSeuil: number;
  valeurStock: number;
  margeMoyenne: number;
  topArticles: TopArticle[];
  ventesParMois: VentesParMois[];
  ventesParCategorie: VentesParCategorie[];
  commandesParClient: CommandesParClient[];
}

export const MOIS_LABELS = [
  'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
  'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'
];
