import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Article } from 'src/app/Models/Article';
import { Category } from 'src/app/Models/Category';
import { ArticleService } from 'src/app/services/article.service';
import { CategoryService } from 'src/app/services/category.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-new-article',
  templateUrl: './new-article.component.html',
  styleUrls: ['./new-article.component.css']
})
export class NewArticleComponent implements OnInit {

  article: Article = {};
  listeArticles: Article[] = [];
  category: Category = {};
  listeCategorys: Category[] = [];
  errorMsg: Array<string> = [];

  /** Photo de l'article */
  photoSelectionnee?: File;
  apercuPhoto?: string;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private articleService: ArticleService,
    private categoryService: CategoryService,
    private userService: UserService) { }

  ngOnInit(): void {
    this.findAllCategories();
    const idArticle = this.activatedRoute.snapshot.params['idarticle'];
    if (idArticle) {
      this.articleService.findArticleById(idArticle).subscribe(
        (responce: Article) => {
          this.article = responce;
          this.category = responce.category!;
        },
        (error: any) => {
          console.log(error);
        }
      );
    }
  }

  findAllCategories(): void {
    this.categoryService.findAll()
      .subscribe((responce: Category[]) => {
        this.listeCategorys = responce;
      }, (error: any) => {
        console.log(error);
      });
  }

  surPhotoChoisie(event: any): void {
    const fichier: File | undefined = event?.target?.files?.[0];
    if (!fichier) {
      return;
    }
    this.photoSelectionnee = fichier;
    const lecteur = new FileReader();
    lecteur.onload = () => { this.apercuPhoto = lecteur.result as string; };
    lecteur.readAsDataURL(fichier);
  }

  afficherPhoto(): string {
    if (this.apercuPhoto) {
      return this.apercuPhoto;
    }
    return this.articleService.photoUrl(this.article.photo) ?? 'assets/product.png';
  }

  enregistrerArticle(): void {
    this.article.category = this.category;

    this.articleService.enregisterArticle(this.article).subscribe(
      (responce: Article) => {
        // Si une photo a été choisie, l'uploader pour l'article enregistré
        if (this.photoSelectionnee && responce.id) {
          this.articleService.updatePhotoArticle(responce.id, this.photoSelectionnee).subscribe(
            () => this.router.navigate(['articles']),
            () => this.router.navigate(['articles']) // article créé, photo en échec : on retourne quand même
          );
        } else {
          this.router.navigate(['articles']);
        }
      },
      (error: any) => {
        if (error && error.error) {
          this.errorMsg = error.error.errors ?? [error.error.message];
          console.log(this.errorMsg);
        }
      }
    );
  }

  claculerPrixTTC() {
    if (this.article.prixUnitaire && this.article.tauxTva) {
      this.article.prixUnitaireTTc =
        +this.article.prixUnitaire + (+(this.article.prixUnitaire * (this.article.tauxTva / 100)));
    }
  }

  cancel(): void {
    this.router.navigate(['articles']);
  }

}
