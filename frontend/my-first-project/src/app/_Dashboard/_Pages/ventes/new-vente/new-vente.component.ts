import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Article } from 'src/app/Models/Article';
import { LigneVente } from 'src/app/Models/LigneVente';
import { Ventes } from 'src/app/Models/Ventes';
import { ArticleService } from 'src/app/services/article.service';
import { VenteService } from 'src/app/services/vente.service';

interface LigneTemporaire {
  article?: Article;
  quantite?: number;
  prixUnitaire?: number;
}

@Component({
  selector: 'app-new-vente',
  templateUrl: './new-vente.component.html',
  styleUrls: ['./new-vente.component.css']
})
export class NewVenteComponent implements OnInit {

  enCours = false;
  errorMsg: string[] = [];

  articles: Article[] = [];

  /** Ligne en cours de saisie */
  articleRecherche = '';
  articlesSuggeres: Article[] = [];
  ligneEnCours: LigneTemporaire = {};
  /** Lignes ajoutées à la vente */
  lignes: LigneTemporaire[] = [];

  constructor(
    private router: Router,
    private articleService: ArticleService,
    private venteService: VenteService
  ) {}

  ngOnInit(): void {
    this.articleService.getAllArticles().subscribe(
      (articles: Article[]) => { this.articles = articles; },
      () => {}
    );
  }

  /** Suggestions d'articles pendant la saisie. */
  surRechercheArticle(): void {
    const terme = this.articleRecherche.trim().toLowerCase();
    if (terme.length < 2) {
      this.articlesSuggeres = [];
      return;
    }
    this.articlesSuggeres = this.articles
      .filter(a => (a.designation ?? '').toLowerCase().includes(terme)
        || (a.codeArticle ?? '').toLowerCase().includes(terme))
      .slice(0, 6);
  }

  choisirArticle(article: Article): void {
    this.ligneEnCours.article = article;
    this.ligneEnCours.prixUnitaire = article.prixUnitaireTTc ?? article.prixUnitaire;
    this.articleRecherche = `${article.codeArticle} — ${article.designation}`;
    this.articlesSuggeres = [];
  }

  ajouterLigne(): void {
    if (!this.ligneEnCours.article || !this.ligneEnCours.quantite) return;
    this.lignes.push({ ...this.ligneEnCours });
    this.ligneEnCours = {};
    this.articleRecherche = '';
  }

  retirerLigne(index: number): void {
    this.lignes.splice(index, 1);
  }

  totalVente(): number {
    return this.lignes.reduce(
      (somme, l) => somme + Number(l.quantite ?? 0) * Number(l.prixUnitaire ?? 0), 0
    );
  }

  cancel(): void {
    this.router.navigate(['ventes']);
  }

  enregistrer(): void {
    this.errorMsg = [];
    if (this.lignes.length === 0) {
      this.errorMsg = ['Ajoutez au moins un article à la vente.'];
      return;
    }

    this.enCours = true;
    const ligneVentes: LigneVente[] = this.lignes.map(l => ({
      article: { id: l.article!.id },
      quantite: l.quantite,
      prixUnitaire: l.prixUnitaire
    }));

    const vente: Ventes = {
      code: 'V-' + Date.now(),
      dateVente: Date.now(),
      commentaire: '',
      ligneVentes
    };

    this.venteService.enregistrerVente(vente).subscribe(
      () => this.cancel(),
      (error: any) => {
        this.enCours = false;
        console.log(error);
        this.errorMsg = error?.error?.errors?.length
          ? error.error.errors
          : [error?.error?.message ?? 'Erreur lors de l\'enregistrement de la vente.'];
      }
    );
  }
}
