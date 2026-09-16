import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Utilisateur } from 'src/app/Models/Utilisateure';
import { UserService } from 'src/app/services/user.service';
import { ColonneTable } from 'src/app/_Dashboard/components/data-table/data-table.component';

interface ActionTable {
  type: 'details' | 'modifier' | 'supprimer';
  ligne: Utilisateur;
}

@Component({
  selector: 'app-utilisateurs',
  templateUrl: './utilisateurs.component.html',
  styleUrls: ['./utilisateurs.component.css']
})
export class UtilisateursComponent implements OnInit {

  listUtilisateur: Utilisateur[] = [];
  chargement = true;
  errorMsg = '';

  colonnes: ColonneTable<Utilisateur>[] = [
    { cle: 'nom', libelle: 'col.nom', triable: true },
    { cle: 'prenom', libelle: 'col.prenom', triable: true },
    { cle: 'email', libelle: 'col.email', triable: false },
    {
      cle: 'roles',
      libelle: 'col.roles',
      triable: false,
      formateur: (u: Utilisateur) =>
        (u.roles ?? []).map(r => r?.rolename ?? '').filter(Boolean).join(', ')
    },
    { cle: 'adresse.Ville', libelle: 'col.ville', triable: true },
    { cle: 'actions', libelle: 'col.actions', actions: true }
  ];

  /** État de la modale de suppression. */
  suppressionOuverte = false;
  messageSuppression = '';
  private idSuppression: number | undefined;

  constructor(private router: Router,
    private userService: UserService) { }

  ngOnInit(): void {
    this.findAllUtilisateurs();
  }

  nouvelUtilisateur(): void {
    this.router.navigate(['/newutilisateur']);
  }

  findAllUtilisateurs(): void {
    this.chargement = true;
    this.errorMsg = '';
    this.userService.findAllUtilisateurs().subscribe(
      (utilisateurs: Utilisateur[]) => {
        this.listUtilisateur = utilisateurs;
        this.chargement = false;
      },
      () => {
        this.errorMsg = 'Erreur lors du chargement des utilisateurs.';
        this.chargement = false;
      }
    );
  }

  /** Ouvre la confirmation de suppression pour un utilisateur. */
  surAction(action: ActionTable): void {
    const utilisateur = action.ligne;
    if (action.type === 'modifier' || action.type === 'details') {
      this.router.navigate(['/newutilisateur', utilisateur.id]);
      return;
    }
    if (action.type === 'supprimer') {
      this.idSuppression = utilisateur.id;
      this.messageSuppression =
        `Êtes-vous sûr de vouloir supprimer ${utilisateur.prenom ?? ''} ${utilisateur.nom ?? ''} ? Cette action est irréversible.`;
      this.suppressionOuverte = true;
    }
  }

  confirmerSuppression(): void {
    if (!this.idSuppression) {
      return;
    }
    this.userService.deleteUtilisateur(this.idSuppression).subscribe(
      () => {
        this.fermerSuppression();
        this.findAllUtilisateurs();
      },
      () => {
        this.fermerSuppression();
        this.errorMsg = 'Suppression impossible.';
        setTimeout(() => { this.errorMsg = ''; }, 5000);
      }
    );
  }

  annulerSuppression(): void {
    this.fermerSuppression();
  }

  private fermerSuppression(): void {
    this.suppressionOuverte = false;
    this.idSuppression = undefined;
    this.messageSuppression = '';
  }
}
