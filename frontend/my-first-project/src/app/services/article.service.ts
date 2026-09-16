import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from './user.service';
import { CategoryService } from './category.service';
import { HttpClient } from '@angular/common/http';
import { Article } from '../Models/Article';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  APP_URL = environment.apiUrl + '/articles';

  constructor(private router : Router,
    private userService: UserService,
    private httpClient: HttpClient) {}

    enregisterArticle(article: Article):Observable<Article> {
      article.idEntreprise = this.userService.getConnectedUser().entreprise?.id;
      return this.httpClient.post<Article>(`${this.APP_URL}/create`,article);
    }

    getAllArticles():Observable<Article[]> {
      return this.httpClient.get<Article[]>(`${this.APP_URL}/all`);
    }

    findArticleById(idArticle: number):Observable<Article> {
      return this.httpClient.get<Article>(`${this.APP_URL}/find/${idArticle}`);
    }

    deleteArticle(idArticle: number):Observable<void>{
      return this.httpClient.delete<void>(`${this.APP_URL}/delete/${idArticle}`);
    }

    /** Upload la photo d'un article (stockage local côté backend). */
    updatePhotoArticle(idArticle: number, fichier: File):Observable<Article>{
      const formData = new FormData();
      formData.append('file', fichier);
      return this.httpClient.post<Article>(`${environment.apiUrl}/photos/article/${idArticle}`, formData);
    }

    /** URL absolue d'une photo servie par le backend (ex. /photos/xxx.jpg). */
    photoUrl(photo?: string): string | undefined {
      if (!photo) {
        return undefined;
      }
      return photo.startsWith('http') ? photo : `${environment.apiUrl.replace(/\/gestiondestock$/, '')}${photo}`;
    }
/*
    enregisterArticle(article: Article):Observable<Article> {
      return this.httpClient.post<Article>(`${this.APP_URL}/`,article);
    }*/
}


