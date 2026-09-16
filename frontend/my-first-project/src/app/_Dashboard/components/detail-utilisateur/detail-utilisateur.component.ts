import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Utilisateur } from 'src/app/Models/Utilisateure';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-detail-utilisateur',
  templateUrl: './detail-utilisateur.component.html',
  styleUrls: ['./detail-utilisateur.component.css']
})
export class DetailUtilisateurComponent {

  @Input()
  utilisateur: Utilisateur = {};

  @Output()
  suppressionResult = new EventEmitter<string>();

  constructor(private userService: UserService) { }

  supprimer(): void {
    if (!this.utilisateur.id) {
      return;
    }
    this.userService.deleteUtilisateur(this.utilisateur.id).subscribe(
      () => this.suppressionResult.emit('success'),
      (error: any) => {
        console.log(error);
        this.suppressionResult.emit('error');
      }
    );
  }

  initiales(): string {
    return ((this.utilisateur.nom ?? '?').charAt(0) + (this.utilisateur.prenom ?? '').charAt(0)).toUpperCase();
  }
}
