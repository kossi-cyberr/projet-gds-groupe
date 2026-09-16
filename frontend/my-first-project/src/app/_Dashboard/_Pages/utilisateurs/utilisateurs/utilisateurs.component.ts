import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Utilisateur } from 'src/app/Models/Utilisateure';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-utilisateurs',
  templateUrl: './utilisateurs.component.html',
  styleUrls: ['./utilisateurs.component.css']
})
export class UtilisateursComponent implements OnInit {

  listUtilisateur: Utilisateur[] = [];
  chargement = true;
  errorMsg = '';

  constructor(private router: Router,
    private userService: UserService) { }

  ngOnInit(): void {
    this.findAllUtilisateurs();
  }

  nouvelUtilisateur(): void {
    this.router.navigate(['/newutilisateur']);
  }

  findAllUtilisateurs(): void {
    this.chargement = true;
    this.userService.findAllUtilisateurs().subscribe(
      (utilisateurs: Utilisateur[]) => {
        this.listUtilisateur = utilisateurs;
        this.chargement = false;
      },
      (error: any) => {
        console.log(error);
        this.errorMsg = 'Erreur lors du chargement des utilisateurs.';
        this.chargement = false;
      }
    );
  }

  surSuppression(resultat: string): void {
    if (resultat === 'success') {
      this.findAllUtilisateurs();
    } else {
      this.errorMsg = 'Suppression impossible.';
      setTimeout(() => { this.errorMsg = ''; }, 5000);
    }
  }

}
