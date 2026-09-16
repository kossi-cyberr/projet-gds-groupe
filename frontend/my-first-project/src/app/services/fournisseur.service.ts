import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Fournisseur } from '../Models/Fournisseur';
import { Observable } from 'rxjs/internal/Observable';
import { UserService } from './user.service';

@Injectable({
  providedIn: 'root'
})
export class FournisseurService {

  APP_URL = environment.apiUrl + '/fournisseurs';

  constructor(private httpClient: HttpClient,
    private userService: UserService) {}

  enregistrerFournisseur(fournisseur: Fournisseur): Observable<Fournisseur> {
    fournisseur.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.httpClient.post<Fournisseur>(`${this.APP_URL}/create`, fournisseur);
  }

  findAll(): Observable<Fournisseur[]> {
    return this.httpClient.get<Fournisseur[]>(`${this.APP_URL}/all`);
  }

  findFournisseurById(idFournisseur: number): Observable<Fournisseur> {
    return this.httpClient.get<Fournisseur>(`${this.APP_URL}/${idFournisseur}`);
  }

  updateFournisseur(id: number, fournisseur: Fournisseur): Observable<Fournisseur> {
    fournisseur.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.httpClient.put<Fournisseur>(`${this.APP_URL}/update/${id}`, fournisseur);
  }

  deleteFournisseur(idFournisseur: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.APP_URL}/delete/${idFournisseur}`);
  }
}
