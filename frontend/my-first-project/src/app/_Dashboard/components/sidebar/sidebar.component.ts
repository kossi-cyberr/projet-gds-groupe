import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd, Event as RouterEvent } from '@angular/router';
import { filter, takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { LanguageService } from 'src/app/services/language.service';

interface ItemMenu {
  id: string;
  /** Clé de traduction (LanguageService). */
  cle: string;
  url: string;
  /** Nom Lucide de l'icône (kebab-case, fournie via LucideAngularModule.pick). */
  icone: string;
}

interface SectionMenu {
  id: string;
  cle: string;
  items: ItemMenu[];
}

/**
 * Sidebar de navigation professionnelle.
 * Sections stables (Pilotage / Gestion / Système), route active mise en évidence,
 * profil utilisateur + déconnexion gérés par le composant hôte (dashboard).
 */
@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit, OnDestroy {
  private detruit$ = new Subject<void>();

  sections: SectionMenu[] = [
    {
      id: 'pilotage',
      cle: 'section.pilotage',
      items: [
        { id: 'dashboard', cle: 'nav.dashboard', url: '', icone: 'layout-dashboard' },
        { id: 'stats', cle: 'nav.stats', url: 'statistique', icone: 'trending-up' }
      ]
    },
    {
      id: 'gestion',
      cle: 'section.gestion',
      items: [
        { id: 'articles', cle: 'nav.articles', url: 'articles', icone: 'package' },
        { id: 'mvtstk', cle: 'nav.mvtstk', url: 'mvtstk', icone: 'arrow-left-right' },
        { id: 'clients', cle: 'nav.clients', url: 'client', icone: 'users' },
        { id: 'cmd-clients', cle: 'nav.cmdClients', url: 'commandeclient', icone: 'shopping-cart' },
        { id: 'fournisseurs', cle: 'nav.fournisseurs', url: 'fournisseurs', icone: 'truck' },
        { id: 'cmd-fournisseurs', cle: 'nav.cmdFournisseurs', url: 'commandefournissuer', icone: 'building-2' }
      ]
    },
    {
      id: 'systeme',
      cle: 'section.systeme',
      items: [
        { id: 'categories', cle: 'nav.categories', url: 'categories', icone: 'tags' },
        { id: 'utilisateurs', cle: 'nav.utilisateurs', url: 'utilisateur', icone: 'user-cog' }
      ]
    }
  ];

  /** Route courante (pour l'état actif des liens). */
  urlCourante = '';

  constructor(
    private router: Router,
    public langueService: LanguageService
  ) {}

  ngOnInit(): void {
    this.urlCourante = this.router.url;
    this.router.events
      .pipe(
        filter((e: RouterEvent) => e instanceof NavigationEnd),
        takeUntil(this.detruit$)
      )
      .subscribe((e: RouterEvent) => {
        this.urlCourante = (e as NavigationEnd).urlAfterRedirects;
      });
  }

  ngOnDestroy(): void {
    this.detruit$.next();
    this.detruit$.complete();
  }

  /** Vrai si l'item correspond à la route active. */
  estActif(item: ItemMenu): boolean {
    if (item.url === '') {
      return this.urlCourante === '/' || this.urlCourante === '';
    }
    const url = '/' + item.url;
    return this.urlCourante === url || this.urlCourante.startsWith(url + '/') || this.urlCourante.startsWith(url + '?');
  }

  naviguer(item: ItemMenu): void {
    this.router.navigate([item.url]);
  }

  /** Traduction directe d'une clé dans le template. */
  t(cle: string): string {
    return this.langueService.t(cle);
  }
}
