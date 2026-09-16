import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-new-utilisteur',
  templateUrl: './new-utilisteur.component.html',
  styleUrls: ['./new-utilisteur.component.css']
})
export class NewUtilisteurComponent {

  enCours = false;
  errorMsg: string[] = [];

  form: any = {
    nom: '',
    prenom: '',
    email: '',
    motDePasse: '',
    dateDeNaissance: '',
    addresse1: '',
    addresse2: '',
    Ville: '',
    codePostale: '',
    pays: '',
    role: 'VENDEUR'
  };

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private userService: UserService) { }

  cancel(): void {
    this.router.navigate(['utilisateur']);
  }

  enregistrer(): void {
    this.errorMsg = [];
    const champsObligatoires = [this.form.nom, this.form.prenom, this.form.email,
      this.form.motDePasse, this.form.dateDeNaissance, this.form.addresse1,
      this.form.Ville, this.form.codePostale, this.form.pays];
    if (champsObligatoires.some(c => !c || !c.trim())) {
      this.errorMsg = ['Tous les champs marqués * sont obligatoires.'];
      return;
    }

    this.enCours = true;
    this.userService.creerUtilisateur({
      nom: this.form.nom,
      prenom: this.form.prenom,
      email: this.form.email,
      motDePasse: this.form.motDePasse,
      dateDeNaissance: this.form.dateDeNaissance,
      adresse: {
        addresse1: this.form.addresse1,
        addresse2: this.form.addresse2,
        Ville: this.form.Ville,
        codePostale: this.form.codePostale,
        pays: this.form.pays
      }
    }).subscribe(
      (utilisateurCree: any) => {
        // Affecter le rôle choisi après la création
        if (utilisateurCree?.id && this.form.role) {
          this.userService.assignerRole(utilisateurCree.id, this.form.role).subscribe(
            () => this.cancel(),
            () => this.cancel() // utilisateur créé mais rôle en échec : on retourne à la liste
          );
        } else {
          this.cancel();
        }
      },
      (error: any) => {
        this.enCours = false;
        console.log(error);
        this.errorMsg = error?.error?.errors?.length
          ? error.error.errors
          : [error?.error?.message ?? 'Erreur lors de la création de l\'utilisateur.'];
      }
    );
  }

}
