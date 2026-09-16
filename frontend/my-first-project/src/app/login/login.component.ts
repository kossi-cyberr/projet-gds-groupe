import { Component } from '@angular/core';
import { AuthenticationRequest } from '../Models/AuthenticationRequest';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { ThemeService } from '../services/theme.service';
import { LanguageService } from '../services/language.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  authenticationRequest: AuthenticationRequest = {};
  errorMessage = '';
  /** État de soumission : bloque le double clic et affiche le spinner. */
  chargement = false;

  constructor(
    private userService: UserService,
    private router: Router,
    public theme: ThemeService,
    public langueService: LanguageService
  ) { }

  ngOnInit(): void {
  }

  login(): void {
    if (this.chargement) {
      return;
    }
    if (!this.authenticationRequest.login?.trim() || !this.authenticationRequest.password) {
      this.errorMessage = 'Veuillez saisir votre email et votre mot de passe.';
      return;
    }
    this.chargement = true;
    this.errorMessage = '';
    this.userService.login(this.authenticationRequest).subscribe(
      (response) => {
        this.userService.setAccessToken(response);
        this.getUserByEmail();
        this.router.navigate(['']);
      },
      () => {
        this.errorMessage = 'Email et / ou mot de passe incorrect.';
        this.chargement = false;
      }
    );
  }

  /** Récupère le profil pour l'affichage (non bloquant si échec). */
  private getUserByEmail(): void {
    this.userService.getUserByEmail(this.authenticationRequest.login!).subscribe(
      (response) => this.userService.setUtilisateur(response),
      () => { /* non bloquant : le profil sera rechargé par le dashboard */ }
    );
  }

  basculerTheme(): void {
    this.theme.toggle();
  }

  get estSombre(): boolean {
    return this.theme.estSombre;
  }

  basculerLangue(): void {
    this.langueService.basculer();
  }

  get langueCourante(): string {
    return this.langueService.langue;
  }

  /** Traduction directe d'une clé dans le template. */
  t(cle: string): string {
    return this.langueService.t(cle);
  }
}
