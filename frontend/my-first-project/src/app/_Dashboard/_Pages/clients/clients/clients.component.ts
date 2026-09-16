import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Client } from 'src/app/Models/Client';
import { ClientService } from 'src/app/services/client.service';
import { ColonneTable } from 'src/app/_Dashboard/components/data-table/data-table.component';

interface ActionTable {
  type: 'details' | 'modifier' | 'supprimer';
  ligne: Client;
}

@Component({
  selector: 'app-clients',
  templateUrl: './clients.component.html',
  styleUrls: ['./clients.component.css']
})
export class ClientsComponent implements OnInit {

  listClient: Client[] = [];
  chargement = true;
  errorMsg = '';

  /** Définition des colonnes du tableau. */
  colonnes: ColonneTable<Client>[] = [
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
    private clientService: ClientService) { }

  ngOnInit(): void {
    this.findAllClients();
  }

  noveauClient(): void {
    this.router.navigate(['newclient']);
  }

  findAllClients(): void {
    this.chargement = true;
    this.errorMsg = '';
    this.clientService.findAll().subscribe(
      (clients: Client[]) => {
        this.listClient = clients;
        this.chargement = false;
      },
      () => {
        this.errorMsg = 'Erreur lors du chargement des clients.';
        this.chargement = false;
      }
    );
  }

  /** Ouvre la confirmation de suppression pour un client. */
  surAction(action: ActionTable): void {
    const client = action.ligne;
    if (action.type === 'modifier' || action.type === 'details') {
      this.router.navigate(['newclient', client.id]);
      return;
    }
    if (action.type === 'supprimer') {
      this.idSuppression = client.id;
      this.messageSuppression =
        `Êtes-vous sûr de vouloir supprimer ${client.prenom ?? ''} ${client.nom ?? ''} ? Cette action est irréversible.`;
      this.suppressionOuverte = true;
    }
  }

  confirmerSuppression(): void {
    if (!this.idSuppression) {
      return;
    }
    this.clientService.deleteClient(this.idSuppression).subscribe(
      () => {
        this.fermerSuppression();
        this.findAllClients();
      },
      () => {
        this.fermerSuppression();
        this.errorMsg = 'Suppression impossible : ce client est peut-être utilisé dans des commandes.';
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
