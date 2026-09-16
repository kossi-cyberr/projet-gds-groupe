import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type Langue = 'fr' | 'en';

/**
 * Dictionnaire de traduction du layout (phase pilote de l'i18n).
 * Les pages métier seront migrées progressivement vers ce service.
 */
const DICO: Record<Langue, Record<string, string>> = {
  fr: {
    'app.clinic': 'Gestion de stock',
    'app.brand': 'Clinique',
    'app.logout': 'Déconnexion',
    'section.pilotage': 'Pilotage',
    'section.gestion': 'Gestion',
    'section.systeme': 'Système',
    'nav.dashboard': 'Tableau de bord',
    'nav.stats': 'Statistiques',
    'nav.articles': 'Articles',
    'nav.mvtstk': 'Mouvements de stock',
    'nav.clients': 'Clients',
    'nav.cmdClients': 'Commandes clients',
    'nav.fournisseurs': 'Fournisseurs',
    'nav.cmdFournisseurs': 'Commandes fournisseurs',
    'nav.categories': 'Catégories',
    'nav.utilisateurs': 'Utilisateurs',
    'nav.profil': 'Mon profil'
  },
  en: {
    'app.clinic': 'Inventory management',
    'app.brand': 'Clinic',
    'app.logout': 'Sign out',
    'section.pilotage': 'Overview',
    'section.gestion': 'Management',
    'section.systeme': 'System',
    'nav.dashboard': 'Dashboard',
    'nav.stats': 'Statistics',
    'nav.articles': 'Articles',
    'nav.mvtstk': 'Stock movements',
    'nav.clients': 'Clients',
    'nav.cmdClients': 'Client orders',
    'nav.fournisseurs': 'Suppliers',
    'nav.cmdFournisseurs': 'Supplier orders',
    'nav.categories': 'Categories',
    'nav.utilisateurs': 'Users',
    'nav.profil': 'My profile'
  }
};

/**
 * Langue applicative (fr / en) avec persistance.
 * Le français reste la langue par défaut du projet.
 */
@Injectable({ providedIn: 'root' })
export class LanguageService {
  private static readonly STORAGE_KEY = 'clinic-lang';

  private courant: Langue;
  private sujet = new BehaviorSubject<Langue>('fr');

  /** Flux de la langue courante (pour réagir aux changements). */
  readonly langue$ = this.sujet.asObservable();

  constructor() {
    const sauvegarde = localStorage.getItem(LanguageService.STORAGE_KEY) as Langue | null;
    this.courant = sauvegarde === 'en' ? 'en' : 'fr';
    this.sujet.next(this.courant);
  }

  get langue(): Langue {
    return this.courant;
  }

  /** Bascule FR <-> EN et persiste le choix. */
  basculer(): void {
    this.courant = this.courant === 'fr' ? 'en' : 'fr';
    localStorage.setItem(LanguageService.STORAGE_KEY, this.courant);
    this.sujet.next(this.courant);
  }

  /** Traduit une clé (retourne la clé elle-même si absente). */
  t(cle: string): string {
    return DICO[this.courant][cle] ?? cle;
  }
}
