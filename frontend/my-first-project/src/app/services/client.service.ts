import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Client } from '../Models/Client';
import { Observable } from 'rxjs/internal/Observable';
import { UserService } from './user.service';

@Injectable({
  providedIn: 'root'
})
export class ClientService {

  APP_URL = environment.apiUrl + '/clients';

  constructor(private httpClient: HttpClient,
    private userService: UserService) {}

  enregistrerClient(client: Client): Observable<Client> {
    client.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.httpClient.post<Client>(`${this.APP_URL}/create`, client);
  }

  findAll(): Observable<Client[]> {
    return this.httpClient.get<Client[]>(`${this.APP_URL}/all`);
  }

  findClientById(idClient: number): Observable<Client> {
    return this.httpClient.get<Client>(`${this.APP_URL}/${idClient}`);
  }

  updateClient(id: number, client: Client): Observable<Client> {
    client.idEntreprise = this.userService.getConnectedUser()?.entreprise?.id;
    return this.httpClient.put<Client>(`${this.APP_URL}/update/${id}`, client);
  }

  deleteClient(idClient: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.APP_URL}/delete/${idClient}`);
  }
}
