import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthenticationRequest } from '../Models/AuthenticationRequest';
import { AuthenticationResponse } from '../Models/AuthenticationResponse';
import { AuthenticationService } from './authentication.service';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Utilisateur } from '../Models/Utilisateure';
import { ChangerPassword } from '../Models/ChangerMotDePasse';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  APP_URL = environment.apiUrl;

  constructor(private authenticationService: AuthenticationService,
    private router: Router,
    private httpClient: HttpClient) { }

  login(authenticationRequest: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.authenticationService.login(authenticationRequest);
  }
  setAccessToken(authenticationResponse: AuthenticationResponse): void {
    localStorage.setItem('accessToken', JSON.stringify(authenticationResponse));
  }

  isUserLoggedAndAccessTokenValid(): boolean {
    if (localStorage.getItem('accessToken')) {
      return true;
    }
    this.router.navigate(['/login']);
    return false;
  }

  getUserByEmail(email: string): Observable<Utilisateur> {
    return this.httpClient.get<Utilisateur>(`${this.APP_URL}/utilisateurs/find/${email}`)
  }

  /** Liste tous les utilisateurs de l'entreprise courante. */
  findAllUtilisateurs(): Observable<Utilisateur[]> {
    return this.httpClient.get<Utilisateur[]>(`${this.APP_URL}/utilisateurs/all`);
  }

  /** Crée un utilisateur (réservé ADMIN côté backend). */
  creerUtilisateur(utilisateur: Utilisateur): Observable<Utilisateur> {
    return this.httpClient.post<Utilisateur>(`${this.APP_URL}/utilisateurs/create`, utilisateur);
  }

  /** Affecte un rôle (ADMIN / MANAGER / VENDEUR) à un utilisateur. */
  assignerRole(idUtilisateur: number, roleName: string): Observable<Utilisateur> {
    return this.httpClient.put<Utilisateur>(`${this.APP_URL}/utilisateurs/roles/${idUtilisateur}/${roleName}`, {});
  }

  /** Supprime un utilisateur (réservé ADMIN côté backend). */
  deleteUtilisateur(idUtilisateur: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.APP_URL}/utilisateurs/delete/${idUtilisateur}`);
  }

  setUtilisateur(utilisateur: Utilisateur):void {
    localStorage.setItem("utilisateur", JSON.stringify(utilisateur));
  }

  getConnectedUser():Utilisateur{
    if(localStorage.getItem("utilisateur")){
      return JSON.parse(localStorage.getItem("utilisateur") as string);
    }
    return {};
  }

  changerMotDePasse(ChangerMotDePasseObject: ChangerPassword):Observable<ChangerPassword>{
    return this.httpClient.post<ChangerPassword>(`${this.APP_URL}/utilisateurs/update/password`,ChangerMotDePasseObject);

  }

  /** Met à jour le profil de l'utilisateur connecté (PUT /utilisateurs/me). */
  updateMonProfil(utilisateur: Utilisateur): Observable<Utilisateur> {
    return this.httpClient.put<Utilisateur>(`${this.APP_URL}/utilisateurs/me`, utilisateur);
  }

  /** Upload la photo de profil de l'utilisateur connecté (POST /utilisateurs/me/photo). */
  updateMaPhoto(fichier: File): Observable<Utilisateur> {
    const formData = new FormData();
    formData.append('file', fichier);
    return this.httpClient.post<Utilisateur>(`${this.APP_URL}/utilisateurs/me/photo`, formData);
  }

  /** URL absolue d'une photo servie par le backend (ex. /photos/xxx.jpg). */
  photoUrl(photo?: string): string | undefined {
    if (!photo) {
      return undefined;
    }
    return photo.startsWith('http') ? photo : `${environment.apiUrl.replace(/\/gestiondestock$/, '')}${photo}`;
  }

  /** Rafraîchit l'utilisateur connecté depuis l'API (/auth/me) et le stocke en local. */
  refreshConnectedUser(): Observable<Utilisateur> {
    return this.httpClient.get<Utilisateur>(`${this.APP_URL}/auth/me`);
  }

  /*testInterceptor(): Observable<string> {
    return this.httpClient.get<string>(environment.apiUrl + "/message");
  }*/
}
