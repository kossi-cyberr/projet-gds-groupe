import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Ventes } from '../Models/Ventes';
import { Observable } from 'rxjs/internal/Observable';
import { UserService } from './user.service';

@Injectable({ providedIn: 'root' })
export class VenteService {

  APP_URL = environment.apiUrl + '/ventes';

  constructor(private httpClient: HttpClient, private userService: UserService) {}

  enregistrerVente(vente: Ventes): Observable<Ventes> {
    vente.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.httpClient.post<Ventes>(`${this.APP_URL}/create`, vente);
  }

  findAll(): Observable<Ventes[]> {
    return this.httpClient.get<Ventes[]>(`${this.APP_URL}/all`);
  }

  findVenteById(id: number): Observable<Ventes> {
    return this.httpClient.get<Ventes>(`${this.APP_URL}/${id}`);
  }

  deleteVente(id: number): Observable<any> {
    return this.httpClient.delete(`${this.APP_URL}/delete/${id}`);
  }
}
