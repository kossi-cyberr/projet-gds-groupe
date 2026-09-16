import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Client } from 'src/app/Models/Client';
import { ClientService } from 'src/app/services/client.service';

@Component({
  selector: 'app-clients',
  templateUrl: './clients.component.html',
  styleUrls: ['./clients.component.css']
})
export class ClientsComponent implements OnInit {

  listClient: Client[] = [];
  chargement = true;
  errorMsg = '';

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
    this.clientService.findAll().subscribe(
      (clients: Client[]) => {
        this.listClient = clients;
        this.chargement = false;
      },
      (error: any) => {
        console.log(error);
        this.errorMsg = 'Erreur lors du chargement des clients.';
        this.chargement = false;
      }
    );
  }

  surSuppression(resultat: string): void {
    if (resultat === 'success') {
      this.findAllClients();
    } else {
      this.errorMsg = 'Suppression impossible : ce client est peut-être utilisé dans des commandes.';
      setTimeout(() => { this.errorMsg = ''; }, 5000);
    }
  }

}
