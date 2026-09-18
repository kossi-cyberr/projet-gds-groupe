import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Ventes } from 'src/app/Models/Ventes';
import { VenteService } from 'src/app/services/vente.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-page-ventes',
  templateUrl: './page-ventes.component.html',
  styleUrls: ['./page-ventes.component.css']
})
export class PageVentesComponent implements OnInit {

  ventes: Ventes[] = [];
  chargement = true;
  errorMsg = '';
  estVendeur = false;

  constructor(private router: Router, private venteService: VenteService, private userService: UserService) {}

  ngOnInit(): void {
    const user = this.userService.getConnectedUser();
    const roles = user?.roles ?? [];
    this.estVendeur = roles.length === 1 && roles[0]?.rolename === 'VENDEUR';
    this.chargerVentes();
  }

  nouvelleVente(): void {
    this.router.navigate(['newvente']);
  }

  chargerVentes(): void {
    this.chargement = true;
    this.venteService.findAll().subscribe(
      (ventes: Ventes[]) => {
        this.ventes = ventes;
        this.chargement = false;
      },
      (error: any) => {
        console.log(error);
        this.errorMsg = 'Erreur lors du chargement des ventes.';
        this.chargement = false;
      }
    );
  }

  supprimer(vente: Ventes): void {
    if (!vente.id) return;
    this.venteService.deleteVente(vente.id).subscribe(
      () => this.chargerVentes(),
      (error: any) => {
        console.log(error);
        this.errorMsg = error?.error?.message ?? 'Suppression impossible.';
        setTimeout(() => { this.errorMsg = ''; }, 5000);
      }
    );
  }

  totalLignes(vente: Ventes): number {
    return (vente.ligneVentes ?? []).length;
  }

  totalMontant(vente: Ventes): number {
    return (vente.ligneVentes ?? []).reduce(
      (somme, l) => somme + Number(l.quantite ?? 0) * Number(l.prixUnitaire ?? 0), 0
    );
  }

  formaterDate(date: any): string {
    if (!date) return '—';
    return new Date(date).toLocaleDateString('fr-FR');
  }
}
