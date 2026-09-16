import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MvtStk } from '../Models/MvtStk';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root'
})
export class MvtStkService {

  APP_URL = environment.apiUrl + '/mvtstk';

  constructor(private httpClient: HttpClient) {}

  /** Tous les mouvements de stock de l'entreprise courante. */
  findAll(): Observable<MvtStk[]> {
    return this.httpClient.get<MvtStk[]>(`${this.APP_URL}/all`);
  }

  /** Mouvements d'un article. */
  mvtStkArticle(idArticle: number): Observable<MvtStk[]> {
    return this.httpClient.get<MvtStk[]>(`${this.APP_URL}/filter/article/${idArticle}`);
  }

  /** Stock réel (cumul entrées - sorties) d'un article. */
  stockReelArticle(idArticle: number): Observable<number> {
    return this.httpClient.get<number>(`${this.APP_URL}/stockreel/${idArticle}`);
  }

  correctionStockPos(mvt: MvtStk): Observable<MvtStk> {
    return this.httpClient.post<MvtStk>(`${this.APP_URL}/correctionpos`, mvt);
  }

  correctionStockNeg(mvt: MvtStk): Observable<MvtStk> {
    return this.httpClient.post<MvtStk>(`${this.APP_URL}/correctionneg`, mvt);
  }

  entreeStock(mvt: MvtStk): Observable<MvtStk> {
    return this.httpClient.post<MvtStk>(`${this.APP_URL}/entree`, mvt);
  }

  sortieStock(mvt: MvtStk): Observable<MvtStk> {
    return this.httpClient.post<MvtStk>(`${this.APP_URL}/sortie`, mvt);
  }
}
