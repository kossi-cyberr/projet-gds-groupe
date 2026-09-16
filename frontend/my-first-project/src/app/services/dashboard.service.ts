import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Dashboard } from '../Models/Dashboard';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  APP_URL = environment.apiUrl;

  constructor(private httpClient: HttpClient) {}

  getDashboard(): Observable<Dashboard> {
    return this.httpClient.get<Dashboard>(`${this.APP_URL}/dashboard`);
  }
}
