import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Fournisseur } from 'src/app/Models/Fournisseur';
import { FournisseurService } from 'src/app/services/fournisseur.service';

@Component({
  selector: 'app-fournisseurs',
  templateUrl: './fournisseurs.component.html',
  styleUrls: ['./fournisseurs.component.css']
})
export class FournisseursComponent implements OnInit {

  listFournisseur: Fournisseur[] = [];
  chargement = true;
  errorMsg = '';

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
    this.fournisseurService.findAll().subscribe(
      (fournisseurs: Fournisseur[]) => {
        this.listFournisseur = fournisseurs;
        this.chargement = false;
      },
      (error: any) => {
        console.log(error);
        this.errorMsg = 'Erreur lors du chargement des fournisseurs.';
        this.chargement = false;
      }
    );
  }

  surSuppression(resultat: string): void {
    if (resultat === 'success') {
      this.findAllFournisseurs();
    } else {
      this.errorMsg = 'Suppression impossible : ce fournisseur est peut-être utilisé dans des commandes.';
      setTimeout(() => { this.errorMsg = ''; }, 5000);
    }
  }

}
