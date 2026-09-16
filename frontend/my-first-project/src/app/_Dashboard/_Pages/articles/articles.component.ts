import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Article } from 'src/app/Models/Article';
import { ArticleService } from 'src/app/services/article.service';

@Component({
  selector: 'app-articles',
  templateUrl: './articles.component.html',
  styleUrls: ['./articles.component.css']
})
export class ArticlesComponent implements OnInit {

  articlesList: Article[] = [];
  articlesFiltres: Article[] = [];
  termeRecherche = '';

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
    this.articleService.getAllArticles().subscribe(
      (response: Article[]) => {
        this.articlesList = response;
        this.appliquerFiltre();
      },
      (error: any) => {
        console.log(error);
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
}
