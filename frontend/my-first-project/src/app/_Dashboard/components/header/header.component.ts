import { Component, DoCheck, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Article } from 'src/app/Models/Article';
import { ArticleService } from 'src/app/services/article.service';
import { UserService } from 'src/app/services/user.service';
import { Utilisateur } from 'src/app/Models/Utilisateure';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements DoCheck, OnInit {
  connectedUser: Utilisateur = {};
  recherche = '';
  suggestions: Article[] = [];
  afficherSuggestions = false;

  constructor(
    private userService: UserService,
    private articleService: ArticleService,
    private router: Router
  ) {}

  ngDoCheck(): void {
    this.connectedUser = this.userService.getConnectedUser();
  }

  ngOnInit(): void {
    this.chargerArticles();
  }

  private tousLesArticles: Article[] = [];

  private chargerArticles(): void {
    this.articleService.getAllArticles().subscribe(
      (articles: Article[]) => { this.tousLesArticles = articles; },
      () => { this.tousLesArticles = []; }
    );
  }

  onRecherche(): void {
    const terme = this.recherche.trim().toLowerCase();
    if (terme.length < 2) {
      this.suggestions = [];
      this.afficherSuggestions = false;
      return;
    }
    this.suggestions = this.tousLesArticles
      .filter(a =>
        (a.designation ?? '').toLowerCase().includes(terme) ||
        (a.codeArticle ?? '').toLowerCase().includes(terme))
      .slice(0, 6);
    this.afficherSuggestions = this.suggestions.length > 0;
  }

  /** Ferme les suggestions après le blur (délai pour laisser le clic passer) */
  fermerSuggestionsPlusTard(): void {
    setTimeout(() => { this.afficherSuggestions = false; }, 150);
  }

  ouvrirArticle(article: Article): void {
    this.afficherSuggestions = false;
    this.recherche = '';
    this.suggestions = [];
    this.router.navigate(['/newarticles', article.id]);
  }

  voirTousLesResultats(): void {
    this.afficherSuggestions = false;
    this.router.navigate(['/articles'], { queryParams: { q: this.recherche } });
  }
}
