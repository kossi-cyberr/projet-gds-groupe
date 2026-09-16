import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CommandeFournisseur } from '../Models/CommandeFournisseur';
import { LigneCommandeFournisseur } from '../Models/LigneCommandeFournisseur';
import { Observable } from 'rxjs/internal/Observable';
import { UserService } from './user.service';

@Injectable({
  providedIn: 'root'
})
export class CommandeFournisseurService {

  APP_URL = environment.apiUrl + '/commandesfournisseurs';

  constructor(private httpClient: HttpClient,
    private userService: UserService) {}

  enregistrerCommande(commande: CommandeFournisseur): Observable<CommandeFournisseur> {
    commande.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.httpClient.post<CommandeFournisseur>(`${this.APP_URL}/create`, commande);
  }

  findAll(): Observable<CommandeFournisseur[]> {
    return this.httpClient.get<CommandeFournisseur[]>(`${this.APP_URL}/all`);
  }

  findCommandeById(idCommande: number): Observable<CommandeFournisseur> {
    return this.httpClient.get<CommandeFournisseur>(`${this.APP_URL}/${idCommande}`);
  }

  /** Lignes d'une commande (le détail n'est pas renvoyé par /all). */
  findLignesCommande(idCommande: number): Observable<LigneCommandeFournisseur[]> {
    return this.httpClient.get<LigneCommandeFournisseur[]>(`${this.APP_URL}/lignesCommande/${idCommande}`);
  }

  changerEtat(idCommande: number, etat: string): Observable<CommandeFournisseur> {
    return this.httpClient.patch<CommandeFournisseur>(`${this.APP_URL}/update/etat/${idCommande}/${etat}`, {});
  }

  deleteCommande(idCommande: number): Observable<any> {
    return this.httpClient.delete(`${this.APP_URL}/delete/${idCommande}`);
  }
}
