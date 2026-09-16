import { Component } from '@angular/core';
import { AdresseDto } from '../Models/AdresseDto';
import { EntrepriseDto } from '../Models/EntrepriseDto';
import { UserService } from '../services/user.service';
import { AuthenticationRequest } from '../Models/AuthenticationRequest';
import { Router } from '@angular/router';
import { EnServiceService } from '../services/en-service.service';
import { ThemeService } from '../services/theme.service';
import { LanguageService } from '../services/language.service';

@Component({
  selector: 'app-inscire',
  templateUrl: './inscire.component.html',
  styleUrls: ['./inscire.component.css']
})
export class InscireComponent {
  constructor(private entrepriseService: EnServiceService,
    private userService: UserService,
    private router: Router,
    public theme: ThemeService,
    public langueService: LanguageService) { }

  entrepriseDto: EntrepriseDto = {};
  adresse: AdresseDto = {};
  errorsMsg: Array<string> = [];
  authenticationRequest: AuthenticationRequest = {};
  /** État de soumission : bloque le double clic et affiche le spinner. */
  chargement = false;

  inscrire(): void {
    if (this.chargement) {
      return;
    }
    this.entrepriseDto.adresse = this.adresse;
    this.chargement = true;
    this.entrepriseService.save(this.entrepriseDto).subscribe(
      (response: EntrepriseDto) => {
        this.errorsMsg = [];
        this.connectEntreprise();
      },
      (error) => {
        this.chargement = false;
        this.errorsMsg = error?.error?.errors ?? ['Une erreur est survenue lors de la création de l\'entreprise.'];
      }
    );
  }

  connectEntreprise(): void {
    // Mot de passe temporaire attendu par le backend (app.entreprise.default-password)
    const authenticationRequest: AuthenticationRequest = {
      login: this.entrepriseDto.email,
      password: 'Admin123!'
    };
    this.userService.login(authenticationRequest)
      .subscribe(response => {
        this.userService.setAccessToken(response);
        this.getUserByEmail(authenticationRequest.login!);
        localStorage.setItem('origin', 'inscription');
        this.router.navigate(['/changermotdepasse']);
      }, () => {
        // La création a réussi mais la connexion auto a échoué : renvoyer au login
        this.chargement = false;
        this.router.navigate(['/login']);
      });
  }

  getUserByEmail(email: string): void {
    this.userService.getUserByEmail(email).subscribe(
      (response) => {
        this.userService.setUtilisateur(response);
        this.chargement = false;
      },
      () => {
        this.chargement = false;
      });
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
}
