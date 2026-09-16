import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from 'src/app/services/user.service';
import { Utilisateur } from 'src/app/Models/Utilisateure';
import { AdresseDto } from 'src/app/Models/AdresseDto';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  utilisateur: Utilisateur = {};
  modeEdition = false;
  enCours = false;
  messageSucces = '';
  messageErreur = '';

  /** Champs du formulaire d'édition */
  form: any = {};
  photoSelectionnee?: File;
  apercuPhoto?: string;

  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.chargerUtilisateur();
  }

  /** Charge l'utilisateur connecté depuis l'API (source de vérité) puis le stocke en local. */
  chargerUtilisateur(): void {
    this.userService.refreshConnectedUser().subscribe(
      (utilisateur: Utilisateur) => {
        this.utilisateur = utilisateur;
        this.userService.setUtilisateur(utilisateur);
      },
      () => {
        // Fallback : données locales si l'API échoue
        this.utilisateur = this.userService.getConnectedUser();
      }
    );
  }

  activerEdition(): void {
    this.form = {
      nom: this.utilisateur.nom ?? '',
      prenom: this.utilisateur.prenom ?? '',
      dateDeNaissance: this.utilisateur.dateDeNaissance ?? '',
      addresse1: this.utilisateur.adresse?.addresse1 ?? '',
      addresse2: this.utilisateur.adresse?.addresse2 ?? '',
      Ville: this.utilisateur.adresse?.Ville ?? '',
      codePostale: this.utilisateur.adresse?.codePostale ?? '',
      pays: this.utilisateur.adresse?.pays ?? ''
    };
    this.modeEdition = true;
    this.messageSucces = '';
    this.messageErreur = '';
  }

  annulerEdition(): void {
    this.modeEdition = false;
    this.photoSelectionnee = undefined;
    this.apercuPhoto = undefined;
    this.messageErreur = '';
  }

  /** Enregistre les infos du profil (PUT /utilisateurs/me). */
  enregistrerProfil(): void {
    if (!this.form.nom || !this.form.prenom) {
      this.messageErreur = 'Le nom et le prénom sont obligatoires.';
      return;
    }
    this.enCours = true;
    this.messageErreur = '';

    const adresse: AdresseDto = {
      addresse1: this.form.addresse1,
      addresse2: this.form.addresse2,
      Ville: this.form.Ville,
      codePostale: this.form.codePostale,
      pays: this.form.pays
    };

    const profil: Utilisateur = {
      nom: this.form.nom,
      prenom: this.form.prenom,
      dateDeNaissance: this.form.dateDeNaissance || undefined,
      adresse
    };

    this.userService.updateMonProfil(profil).subscribe(
      (utilisateur: Utilisateur) => {
        this.utilisateur = utilisateur;
        this.userService.setUtilisateur(utilisateur);
        this.modeEdition = false;
        this.enCours = false;
        this.messageSucces = 'Profil mis à jour avec succès.';
        this.effacerMessagePlusTard();
      },
      (error: any) => {
        this.enCours = false;
        this.messageErreur = error?.error?.message ?? 'Erreur lors de la mise à jour du profil.';
      }
    );
  }

  /** Capture le fichier sélectionné et affiche un aperçu local. */
  surPhotoChoisie(event: any): void {
    const fichier: File | undefined = event?.target?.files?.[0];
    if (!fichier) {
      return;
    }
    this.photoSelectionnee = fichier;
    const lecteur = new FileReader();
    lecteur.onload = () => { this.apercuPhoto = lecteur.result as string; };
    lecteur.readAsDataURL(fichier);
  }

  /** Upload la photo de profil (POST /utilisateurs/me/photo). */
  enregistrerPhoto(): void {
    if (!this.photoSelectionnee) {
      return;
    }
    this.enCours = true;
    this.messageErreur = '';

    this.userService.updateMaPhoto(this.photoSelectionnee).subscribe(
      (utilisateur: Utilisateur) => {
        this.utilisateur = utilisateur;
        this.userService.setUtilisateur(utilisateur);
        this.photoSelectionnee = undefined;
        this.apercuPhoto = undefined;
        this.enCours = false;
        this.messageSucces = 'Photo de profil mise à jour.';
        this.effacerMessagePlusTard();
      },
      (error: any) => {
        this.enCours = false;
        this.messageErreur = error?.error?.message ?? 'Erreur lors de l\'envoi de la photo.';
      }
    );
  }

  afficherPhoto(): string {
    const url = this.userService.photoUrl(this.utilisateur.photo);
    return url ?? 'assets/product.png';
  }

  initiales(): string {
    return ((this.utilisateur.nom ?? 'U').charAt(0) + (this.utilisateur.prenom ?? '').charAt(0)).toUpperCase();
  }

  adresseComplete(): string {
    const a = this.utilisateur.adresse;
    if (!a) {
      return '—';
    }
    return [a.addresse1, a.addresse2, a.Ville, a.codePostale, a.pays].filter(Boolean).join(', ') || '—';
  }

  private effacerMessagePlusTard(): void {
    setTimeout(() => { this.messageSucces = ''; }, 4000);
  }

  modifierMotDePasse(): void {
    this.router.navigate(['/changermotdepasse']);
  }
}
