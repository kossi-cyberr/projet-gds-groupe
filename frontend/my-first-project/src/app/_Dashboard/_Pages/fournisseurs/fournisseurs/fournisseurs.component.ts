import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Fournisseur } from 'src/app/Models/Fournisseur';
import { FournisseurService } from 'src/app/services/fournisseur.service';
import { ColonneTable } from 'src/app/_Dashboard/components/data-table/data-table.component';

interface ActionTable {
  type: 'details' | 'modifier' | 'supprimer';
  ligne: Fournisseur;
}

@Component({
  selector: 'app-fournisseurs',
  templateUrl: './fournisseurs.component.html',
  styleUrls: ['./fournisseurs.component.css']
})
export class FournisseursComponent implements OnInit {

  listFournisseur: Fournisseur[] = [];
  chargement = true;
  errorMsg = '';

  /** Définition des colonnes du tableau. */
  colonnes: ColonneTable<Fournisseur>[] = [
    { cle: 'nom', libelle: 'col.nom', triable: true },
    { cle: 'prenom', libelle: 'col.prenom', triable: true },
    { cle: 'numTel', libelle: 'col.telephone', triable: false },
    { cle: 'mail', libelle: 'col.email', triable: false },
    { cle: 'adresse.Ville', libelle: 'col.ville', triable: true },
    { cle: 'adresse.pays', libelle: 'col.pays', triable: true },
    { cle: 'actions', libelle: 'col.actions', actions: true }
  ];

  /** État de la modale de suppression. */
  suppressionOuverte = false;
  messageSuppression = '';
  private idSuppression: number | undefined;

  constructor(private router: Router,
    private fournisseurService: FournisseurService) { }

  ngOnInit(): void {
    this.findAllFournisseurs();
  }

  noveauFournissuer(): void {
    this.router.navigate(['newfournisseur']);
  }

  findAllFournisseurs(): void {
    this.chargement = true;
    this.errorMsg = '';
    this.fournisseurService.findAll().subscribe(
      (fournisseurs: Fournisseur[]) => {
        this.listFournisseur = fournisseurs;
        this.chargement = false;
      },
      () => {
        this.errorMsg = 'Erreur lors du chargement des fournisseurs.';
        this.chargement = false;
      }
    );
  }

  /** Ouvre la confirmation de suppression pour un fournisseur. */
  surAction(action: ActionTable): void {
    const fournisseur = action.ligne;
    if (action.type === 'modifier' || action.type === 'details') {
      this.router.navigate(['newfournisseur', fournisseur.id]);
      return;
    }
    if (action.type === 'supprimer') {
      this.idSuppression = fournisseur.id;
      this.messageSuppression =
        `Êtes-vous sûr de vouloir supprimer ${fournisseur.prenom ?? ''} ${fournisseur.nom ?? ''} ? Cette action est irréversible.`;
      this.suppressionOuverte = true;
    }
  }

  confirmerSuppression(): void {
    if (!this.idSuppression) {
      return;
    }
    this.fournisseurService.deleteFournisseur(this.idSuppression).subscribe(
      () => {
        this.fermerSuppression();
        this.findAllFournisseurs();
      },
      () => {
        this.fermerSuppression();
        this.errorMsg = 'Suppression impossible : ce fournisseur est peut-être utilisé dans des commandes.';
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
