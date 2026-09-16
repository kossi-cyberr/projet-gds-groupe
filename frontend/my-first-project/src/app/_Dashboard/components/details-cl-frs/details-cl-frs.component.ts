import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { Client } from 'src/app/Models/Client';
import { Fournisseur } from 'src/app/Models/Fournisseur';
import { ClientService } from 'src/app/services/client.service';
import { FournisseurService } from 'src/app/services/fournisseur.service';

/**
 * Ligne de détail réutilisable pour un client OU un fournisseur.
 * origin : 'client' | 'fournisseur'
 */
@Component({
  selector: 'app-details-cl-frs',
  templateUrl: './details-cl-frs.component.html',
  styleUrls: ['./details-cl-frs.component.css']
})
export class DetailsClFrsComponent {

  @Input()
  origin: 'client' | 'fournisseur' = 'client';

  @Input()
  partenaire: Client | Fournisseur = {};

  @Output()
  suppressionResult = new EventEmitter<string>();

  constructor(
    private clientService: ClientService,
    private fournisseurService: FournisseurService,
    private router: Router
  ) { }

  get estClient(): boolean {
    return this.origin === 'client';
  }

  modifier(): void {
    const route = this.estClient ? 'newclient' : 'newfournisseur';
    this.router.navigate([route, this.partenaire.id]);
  }

  confirmerSuppression(): void {
    if (!this.partenaire.id) {
      return;
    }
    if (this.estClient) {
      this.clientService.deleteClient(this.partenaire.id).subscribe(
        () => this.suppressionResult.emit('success'),
        (error: any) => {
          console.log(error);
          this.suppressionResult.emit('error');
        }
      );
    } else {
      this.fournisseurService.deleteFournisseur(this.partenaire.id).subscribe(
        () => this.suppressionResult.emit('success'),
        (error: any) => {
          console.log(error);
          this.suppressionResult.emit('error');
        }
      );
    }
  }

  adresseComplete(): string {
    const a = this.partenaire.adresse;
    if (!a) {
      return '—';
    }
    return [a.addresse1, a.addresse2, a.Ville, a.codePostale, a.pays].filter(Boolean).join(', ') || '—';
  }

  initiales(): string {
    return ((this.partenaire.nom ?? '?').charAt(0) + (this.partenaire.prenom ?? '').charAt(0)).toUpperCase();
  }
}
