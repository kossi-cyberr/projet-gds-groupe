import { Component, OnInit } from '@angular/core';
import { Article } from 'src/app/Models/Article';
import { MvtStk } from 'src/app/Models/MvtStk';
import { ArticleService } from 'src/app/services/article.service';
import { MvtStkService } from 'src/app/services/mvt-stk.service';

interface ArticleAvecStock {
  article: Article;
  stockReel: number;
}

@Component({
  selector: 'app-mvt-stk',
  templateUrl: './mvt-stk.component.html',
  styleUrls: ['./mvt-stk.component.css']
})
export class MvtStkComponent implements OnInit {

  articlesAvecStock: ArticleAvecStock[] = [];
  /** Historique des mouvements de l'article déployé */
  mouvementsParArticle: { [idArticle: number]: MvtStk[] } = {};
  chargement = true;
  errorMsg = '';

  /** Correction de stock */
  articleACorriger?: Article;
  quantiteCorrection?: number;
  typeCorrection: 'CORRECTION_POS' | 'CORRECTION_NEG' = 'CORRECTION_POS';
  enCoursCorrection = false;

  constructor(private articleService: ArticleService,
    private mvtStkService: MvtStkService) { }

  ngOnInit(): void {
    this.chargerArticles();
  }

  chargerArticles(): void {
    this.chargement = true;
    this.articleService.getAllArticles().subscribe(
      (articles: Article[]) => {
        this.articlesAvecStock = articles.map(article => ({ article, stockReel: 0 }));
        this.chargement = false;
        // Charger les stocks réels
        this.articlesAvecStock.forEach(item => {
          this.mvtStkService.stockReelArticle(item.article.id!).subscribe(
            (stock: number) => { item.stockReel = Number(stock) || 0; },
            () => { item.stockReel = 0; }
          );
        });
      },
      (error: any) => {
        console.log(error);
        this.errorMsg = 'Erreur lors du chargement des articles.';
        this.chargement = false;
      }
    );
  }

  /** Charge l'historique d'un article au déploiement de l'accordéon. */
  chargerMouvements(item: ArticleAvecStock): void {
    const idArticle = item.article.id!;
    if (this.mouvementsParArticle[idArticle]) {
      return;
    }
    this.mvtStkService.mvtStkArticle(idArticle).subscribe(
      (mouvements: MvtStk[]) => { this.mouvementsParArticle[idArticle] = mouvements; },
      (error: any) => console.log(error)
    );
  }

  ouvrirCorrection(item: ArticleAvecStock): void {
    this.articleACorriger = item.article;
    this.quantiteCorrection = undefined;
    this.typeCorrection = 'CORRECTION_POS';
  }

  enregistrerCorrection(): void {
    if (!this.articleACorriger?.id || !this.quantiteCorrection || this.quantiteCorrection <= 0) {
      return;
    }
    this.enCoursCorrection = true;
    const mvt: MvtStk = {
      article: { id: this.articleACorriger.id },
      quantite: this.quantiteCorrection
    };
    const requete = this.typeCorrection === 'CORRECTION_POS'
      ? this.mvtStkService.correctionStockPos(mvt)
      : this.mvtStkService.correctionStockNeg(mvt);
    requete.subscribe(
      () => {
        this.enCoursCorrection = false;
        // Réinitialiser l'historique pour forcer le rechargement + recharger les stocks
        delete this.mouvementsParArticle[this.articleACorriger!.id!];
        this.chargerArticles();
        this.articleACorriger = undefined;
      },
      (error: any) => {
        this.enCoursCorrection = false;
        console.log(error);
        this.errorMsg = error?.error?.message ?? 'Correction impossible.';
        setTimeout(() => { this.errorMsg = ''; }, 5000);
      }
    );
  }

  formaterDate(date: any): string {
    if (!date) {
      return '—';
    }
    return new Date(date).toLocaleString('fr-FR');
  }

  typeMvtAffichage(type?: string): string {
    switch (type) {
      case 'ENTREE': return 'Entrée';
      case 'SORTIE': return 'Sortie';
      case 'CORRECTION_POS': return 'Correction +';
      case 'CORRECTION_NEG': return 'Correction -';
      default: return type ?? '—';
    }
  }

  sourceAffichage(source?: string): string {
    switch (source) {
      case 'COMMANDE_CLIENT': return 'Commande client';
      case 'COMMANDE_FOURNISSEUR': return 'Commande fournisseur';
      case 'VENTE': return 'Vente';
      default: return source ?? '—';
    }
  }
}
