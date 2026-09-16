import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AdresseDto } from 'src/app/Models/AdresseDto';
import { Client } from 'src/app/Models/Client';
import { Fournisseur } from 'src/app/Models/Fournisseur';
import { ClientService } from 'src/app/services/client.service';
import { FournisseurService } from 'src/app/services/fournisseur.service';

@Component({
  selector: 'app-new-clt-frs',
  templateUrl: './new-clt-frs.component.html',
  styleUrls: ['./new-clt-frs.component.css']
})
export class NewCltFrsComponent implements OnInit {

  origin = '';
  idAmodifier?: number;
  enCours = false;
  errorMsg: string[] = [];

  form: any = {
    nom: '',
    prenom: '',
    mail: '',
    numTel: '',
    addresse1: '',
    addresse2: '',
    Ville: '',
    codePostale: '',
    pays: ''
  };

  constructor(private activatedRoute: ActivatedRoute,
    private router: Router,
    private clientService: ClientService,
    private fournisseurService: FournisseurService) { }

  ngOnInit(): void {
    // Récupérer l'objet origine dans app-routing.ts
    this.activatedRoute.data.subscribe((data: any) => {
      this.origin = data.origin;
    });
    // Mode édition si un :id est présent dans l'URL
    this.activatedRoute.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.idAmodifier = +id;
        this.chargerPartenaire();
      }
    });
  }

  get estClient(): boolean {
    return this.origin === 'client';
  }

  private chargerPartenaire(): void {
    if (this.estClient) {
      this.clientService.findClientById(this.idAmodifier!).subscribe(
        (client: Client) => this.remplirForm(client),
        (error: any) => console.log(error)
      );
    } else {
      this.fournisseurService.findFournisseurById(this.idAmodifier!).subscribe(
        (fournisseur: Fournisseur) => this.remplirForm(fournisseur),
        (error: any) => console.log(error)
      );
    }
  }

  private remplirForm(partenaire: Client | Fournisseur): void {
    this.form = {
      nom: partenaire.nom ?? '',
      prenom: partenaire.prenom ?? '',
      mail: partenaire.mail ?? '',
      numTel: partenaire.numTel ?? '',
      addresse1: partenaire.adresse?.addresse1 ?? '',
      addresse2: partenaire.adresse?.addresse2 ?? '',
      Ville: partenaire.adresse?.Ville ?? '',
      codePostale: partenaire.adresse?.codePostale ?? '',
      pays: partenaire.adresse?.pays ?? ''
    };
  }

  cancelClick(): void {
    this.router.navigate([this.estClient ? '/client' : '/fournisseurs']);
  }

  enregistrer(): void {
    this.errorMsg = [];
    if (!this.form.nom || !this.form.addresse1 || !this.form.Ville || !this.form.pays) {
      this.errorMsg = ['Nom, adresse 1, ville et pays sont obligatoires.'];
      return;
    }

    const adresse: AdresseDto = {
      addresse1: this.form.addresse1,
      addresse2: this.form.addresse2,
      Ville: this.form.Ville,
      codePostale: this.form.codePostale,
      pays: this.form.pays
    };

    this.enCours = true;
    if (this.estClient) {
      const client: Client = {
        id: this.idAmodifier,
        nom: this.form.nom,
        prenom: this.form.prenom,
        mail: this.form.mail,
        numTel: this.form.numTel,
        adresse
      };
      const requete = this.idAmodifier
        ? this.clientService.updateClient(this.idAmodifier, client)
        : this.clientService.enregistrerClient(client);
      requete.subscribe(
        () => this.cancelClick(),
        (error: any) => this.traiterErreur(error)
      );
    } else {
      const fournisseur: Fournisseur = {
        id: this.idAmodifier,
        nom: this.form.nom,
        prenom: this.form.prenom,
        mail: this.form.mail,
        numTel: this.form.numTel,
        adresse
      };
      const requete = this.idAmodifier
        ? this.fournisseurService.updateFournisseur(this.idAmodifier, fournisseur)
        : this.fournisseurService.enregistrerFournisseur(fournisseur);
      requete.subscribe(
        () => this.cancelClick(),
        (error: any) => this.traiterErreur(error)
      );
    }
  }

  private traiterErreur(error: any): void {
    this.enCours = false;
    console.log(error);
    this.errorMsg = error?.error?.errors?.length
      ? error.error.errors
      : [error?.error?.message ?? 'Erreur lors de l\'enregistrement.'];
  }

}
