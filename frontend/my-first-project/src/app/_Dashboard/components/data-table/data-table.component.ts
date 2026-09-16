import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';

export interface ColonneTable<T> {
  /** Propriété de l'objet à afficher (supporte les chemins "a.b"). */
  cle: string;
  /** Clé de traduction du libellé d'en-tête. */
  libelle: string;
  /** Tri activé sur cette colonne. */
  triable?: boolean;
  /** La valeur est un montant (formatage monnaie). */
  monnaie?: boolean;
  /** Alignement à droite (chiffres). */
  droite?: boolean;
  /** La colonne contient des actions (pas de valeur affichée). */
  actions?: boolean;
  /** Formateur personnalisé de la cellule (ex. liste de rôles). */
  formateur?: (ligne: T) => string;
  /** Largeur suggérée (classe Tailwind). */
  largeur?: string;
}

/**
 * Tableau de données professionnel réutilisable :
 * recherche instantanée, tri par colonne, pagination, états loading /
 * erreur / vide, responsive (scroll horizontal maîtrisé sur mobile).
 */
@Component({
  selector: 'app-data-table',
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.css']
})
export class DataTableComponent<T> implements OnChanges {

  constructor(private langueService: LanguageService) {}

  /** Lignes brutes (non paginées) — le composant gère recherche/tri/pagination localement. */
  @Input() lignes: T[] = [];

  /** Définition des colonnes. */
  @Input() colonnes: ColonneTable<T>[] = [];

  /** Clé de traduction du nom de l'entité (ex. "entite.client"). */
  @Input() entite = 'element';

  /** Déclenché au clic sur « Nouveau » dans l'empty state. */
  @Output() creer = new EventEmitter<void>();

  /** Déclenché au clic sur « Réessayer » (erreur de chargement). */
  @Output() reessayer = new EventEmitter<void>();

  /** Déclenché au clic sur une action de ligne. */
  @Output() action = new EventEmitter<{ type: 'details' | 'modifier' | 'supprimer'; ligne: T }>();

  // --- États d'affichage ---
  @Input() chargement = false;
  /** Une erreur de chargement a eu lieu (affiche l'état erreur). */
  @Input() erreur = false;
  @Input() erreurMessage = 'Erreur lors du chargement.';

  // --- Recherche / tri / pagination ---
  recherche = '';
  colonneTri = '';
  directionTri: 'asc' | 'desc' = 'asc';
  page = 1;
  taille = 10;
  readonly tailles = [5, 10, 25, 50];

  /** Résultats de la recherche (avant tri/pagination). */
  filtres: T[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['lignes']) {
      this.appliquerFiltre();
    }
  }

  /** Filtre les lignes selon la recherche (toutes colonnes textuelles). */
  appliquerFiltre(): void {
    const terme = this.recherche.trim().toLowerCase();
    if (!terme) {
      this.filtres = [...this.lignes];
      return;
    }
    this.filtres = this.lignes.filter(ligne =>
      this.colonnes
        .filter(c => !c.actions)
        .some(c => String(this.valeurDe(ligne, c.cle) ?? '').toLowerCase().includes(terme))
    );
    this.page = 1;
  }

  /** Tri par colonne (clic : asc → desc → asc…). */
  trier(colonne: ColonneTable<T>): void {
    if (!colonne.triable) {
      return;
    }
    if (this.colonneTri === colonne.cle) {
      this.directionTri = this.directionTri === 'asc' ? 'desc' : 'asc';
    } else {
      this.colonneTri = colonne.cle;
      this.directionTri = 'asc';
    }
  }

  /** Valeur brute d'une cellule (support des chemins "a.b"). */
  valeurDe(ligne: T, cle: string): unknown {
    let courant: unknown = ligne;
    for (const part of cle.split('.')) {
      if (courant == null) {
        return null;
      }
      courant = (courant as Record<string, unknown>)[part];
    }
    return courant;
  }

  /** Valeur formatée pour l'affichage. */
  afficher(ligne: T, colonne: ColonneTable<T>): string {
    if (colonne.formateur) {
      return colonne.formateur(ligne) || '—';
    }
    const v = this.valeurDe(ligne, colonne.cle);
    if (v == null || v === '') {
      return '—';
    }
    if (colonne.monnaie) {
      return Number(v).toLocaleString('fr-FR', { maximumFractionDigits: 0 }) + ' F';
    }
    return String(v);
  }

  /** Lignes de la page courante (après filtre + tri). */
  get lignesPage(): T[] {
    const debut = (this.page - 1) * this.taille;
    return this.triees.slice(debut, debut + this.taille);
  }

  /** Actions affichées dans la colonne actions (toutes par défaut). */
  @Input() actionsVisibles: Array<'details' | 'modifier' | 'supprimer'> = ['details', 'modifier', 'supprimer'];

  /** Vrai si l'action donnée doit être affichée. */
  actionDisponible(type: 'details' | 'modifier' | 'supprimer'): boolean {
    return this.actionsVisibles.includes(type);
  }

  /** Libellé traduit d'une colonne. */
  libelleTraduit(colonne: ColonneTable<T>): string {
    return this.langueService.t(colonne.libelle);
  }

  /** Dernier rang affiché sur la page courante (pour « X–Y sur N »). */
  get finPage(): number {
    return Math.min(this.page * this.taille, this.totalFiltre);
  }

  /** Pages visibles : fenêtre glissante de 5 autour de la page courante. */
  get pagesVisibles(): number[] {
    const total = this.pages.length;
    const debut = Math.max(1, Math.min(this.page - 2, total - 4));
    const fin = Math.min(total, debut + 4);
    return Array.from({ length: fin - debut + 1 }, (_, i) => debut + i);
  }

  /** Lignes filtrées puis triées. */
  get triees(): T[] {
    if (!this.colonneTri) {
      return this.filtres;
    }
    const copie = [...this.filtres];
    copie.sort((a, b) => {
      const va = this.valeurDe(a, this.colonneTri);
      const vb = this.valeurDe(b, this.colonneTri);
      const na = Number(va);
      const nb = Number(vb);
      let cmp: number;
      if (!Number.isNaN(na) && !Number.isNaN(nb) && va !== '' && vb !== '') {
        cmp = na - nb;
      } else {
        cmp = String(va ?? '').localeCompare(String(vb ?? ''), 'fr', { sensitivity: 'base' });
      }
      return this.directionTri === 'asc' ? cmp : -cmp;
    });
    return copie;
  }

  get totalFiltre(): number {
    return this.filtres.length;
  }

  get pages(): number[] {
    const total = Math.max(1, Math.ceil(this.totalFiltre / this.taille));
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  changerPage(p: number): void {
    if (p >= 1 && p <= this.pages.length) {
      this.page = p;
    }
  }

  allerPremierePage(): void {
    this.changerPage(1);
  }

  allerDernierePage(): void {
    this.changerPage(this.pages.length);
  }

  pagePrecedente(): void {
    this.changerPage(this.page - 1);
  }

  pageSuivante(): void {
    this.changerPage(this.page + 1);
  }

  emettreAction(type: 'details' | 'modifier' | 'supprimer', ligne: T): void {
    this.action.emit({ type, ligne });
  }
}
