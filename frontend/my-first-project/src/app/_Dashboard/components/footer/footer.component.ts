import { Component } from '@angular/core';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent {

  annee = new Date().getFullYear();
  entreprise = this.userService.getConnectedUser()?.entreprise;

  constructor(private userService: UserService) {}

  connectedUserNom(): string {
    const u = this.userService.getConnectedUser();
    return [u?.prenom, u?.nom].filter(Boolean).join(' ') || u?.email || 'Utilisateur';
  }

}
