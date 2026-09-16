import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Article } from 'src/app/Models/Article';
import { ArticleService } from 'src/app/services/article.service';
import { ColonneTable } from 'src/app/_Dashboard/components/data-table/data-table.component';

interface ActionTable {
  type: 'details' | 'modifier' | 'supprimer';
  ligne: Article;
}

@Component({
  selector: 'app-articles',
  templateUrl: './articles.component.html',
  styleUrls: ['./articles.component.css']
})
export class ArticlesComponent implements OnInit {

  articlesList: Article[] = [];
  articlesFiltres: Article[] = [];
  /** Terme venant de la recherche globale du header (?q=...) */
  termeRecherche = '';
  chargement = true;
  errorMsg = '';

  colonnes: ColonneTable<Article>[] = [
    { cle: 'codeArticle', libelle: 'col.code', triable: true },
    { cle: 'designation', libelle: 'col.designation', triable: true },
    { cle: 'category.designation', libelle: 'col.categorie', triable: true },
    { cle: 'prixUnitaireTTc', libelle: 'col.prixTtc', triable: true, monnaie: true, droite: true },
    { cle: 'seuilAlerte', libelle: 'col.seuil', triable: true, droite: true },
    { cle: 'actions', libelle: 'col.actions', actions: true }
  ];

  /** État de la modale de suppression. */
  suppressionOuverte = false;
  messageSuppression = '';
  private idSuppression: number | undefined;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private articleService: ArticleService) {}

  ngOnInit(): void {
    // Terme venant de la recherche globale du header (?q=...)
    this.route.queryParams.subscribe(params => {
      this.termeRecherche = params['q'] ?? '';
      this.appliquerFiltre();
    });
    this.findAllArticle();
  }

  nouvelArticle(): void {
    this.router.navigate(['newarticles']);
  }

  findAllArticle(): void {
    this.chargement = true;
    this.errorMsg = '';
    this.articleService.getAllArticles().subscribe(
      (response: Article[]) => {
        this.articlesList = response;
        this.appliquerFiltre();
        this.chargement = false;
      },
      () => {
        this.errorMsg = 'Erreur lors du chargement des articles.';
        this.chargement = false;
      }
    );
  }

  /** Filtre local sur la désignation ou le code (recherche header + filtre instantané) */
  appliquerFiltre(): void {
    const terme = (this.termeRecherche ?? '').trim().toLowerCase();
    this.articlesFiltres = terme.length === 0
      ? this.articlesList
      : this.articlesList.filter(a =>
          (a.designation ?? '').toLowerCase().includes(terme) ||
          (a.codeArticle ?? '').toLowerCase().includes(terme));
  }

  surAction(action: ActionTable): void {
    const article = action.ligne;
    if (action.type === 'details' || action.type === 'modifier') {
      this.router.navigate(['newarticles', article.id]);
      return;
    }
    if (action.type === 'supprimer') {
      this.idSuppression = article.id;
      this.messageSuppression =
        `Êtes-vous sûr de vouloir supprimer l'article « ${article.designation ?? article.codeArticle ?? ''} » ? Cette action est irréversible.`;
      this.suppressionOuverte = true;
    }
  }

  confirmerSuppression(): void {
    if (!this.idSuppression) {
      return;
    }
    this.articleService.deleteArticle(this.idSuppression).subscribe(
      () => {
        this.fermerSuppression();
        this.findAllArticle();
      },
      () => {
        this.fermerSuppression();
        this.errorMsg = 'Suppression impossible : cet article est peut-être utilisé dans des commandes ou mouvements.';
        setTimeout(() => { this.errorMsg = ''; }, 5000);
      }
    );
  }

  annulerSuppression(): void {
    this.fermerSuppression();
  }

  private fermerSuppression(): void {
    this.suppressionOuverte = false;
    this.idSuppression = undefined;
    this.messageSuppression = '';
  }
}
