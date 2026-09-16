import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Article } from 'src/app/Models/Article';
import { Client } from 'src/app/Models/Client';
import { CommandeClient } from 'src/app/Models/CommandeClient';
import { CommandeFournisseur } from 'src/app/Models/CommandeFournisseur';
import { Fournisseur } from 'src/app/Models/Fournisseur';
import { LigneCommandeClient } from 'src/app/Models/LigneCommandeClient';
import { LigneCommandeFournisseur } from 'src/app/Models/LigneCommandeFournisseur';
import { ArticleService } from 'src/app/services/article.service';
import { ClientService } from 'src/app/services/client.service';
import { CommandeClientService } from 'src/app/services/commande-client.service';
import { CommandeFournisseurService } from 'src/app/services/commande-fournisseur.service';
import { FournisseurService } from 'src/app/services/fournisseur.service';

interface LigneTemporaire {
  article?: Article;
  quantite?: number;
  prixUnitaire?: number;
}

@Component({
  selector: 'app-new-cmd-clt-frs',
  templateUrl: './new-cmd-clt-frs.component.html',
  styleUrls: ['./new-cmd-clt-frs.component.css']
})
export class NewCmdCltFrsComponent implements OnInit {

  origin = '';
  enCours = false;
  errorMsg: string[] = [];

  clients: Client[] = [];
  fournisseurs: Fournisseur[] = [];
  articles: Article[] = [];

  idPartenaireChoisi?: number;
  partenaireChoisi?: Client | Fournisseur;

  /** Ligne en cours de saisie */
  articleRecherche = '';
  articlesSuggeres: Article[] = [];
  ligneEnCours: LigneTemporaire = {};
  /** Lignes ajoutées à la commande */
  lignes: LigneTemporaire[] = [];

  constructor(private activatedRoute: ActivatedRoute,
    private router: Router,
    private clientService: ClientService,
    private fournisseurService: FournisseurService,
    private articleService: ArticleService,
    private commandeClientService: CommandeClientService,
    private commandeFournisseurService: CommandeFournisseurService) { }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe((data: any) => {
      this.origin = data.origin;
      this.chargerDonnees();
    });
  }

  get estClient(): boolean {
    return this.origin === 'client';
  }

  private chargerDonnees(): void {
    if (this.estClient) {
      this.clientService.findAll().subscribe((clients: Client[]) => { this.clients = clients; }, () => {});
    } else {
      this.fournisseurService.findAll().subscribe((fournisseurs: Fournisseur[]) => { this.fournisseurs = fournisseurs; }, () => {});
    }
    this.articleService.getAllArticles().subscribe((articles: Article[]) => { this.articles = articles; }, () => {});
  }

  /** Met à jour le partenaire sélectionné depuis le <select>. */
  surPartenaireChange(): void {
    const liste: any[] = this.estClient ? this.clients : this.fournisseurs;
    this.partenaireChoisi = liste.find(p => p.id === Number(this.idPartenaireChoisi));
  }

  /** Suggestions d'articles pendant la saisie. */
  surRechercheArticle(): void {
    const terme = this.articleRecherche.trim().toLowerCase();
    if (terme.length < 2) {
      this.articlesSuggeres = [];
      return;
    }
    this.articlesSuggeres = this.articles
      .filter(a => (a.designation ?? '').toLowerCase().includes(terme)
        || (a.codeArticle ?? '').toLowerCase().includes(terme))
      .slice(0, 6);
  }

  choisirArticle(article: Article): void {
    this.ligneEnCours.article = article;
    this.ligneEnCours.prixUnitaire = article.prixUnitaireTTc ?? article.prixUnitaire;
    this.articleRecherche = `${article.codeArticle} — ${article.designation}`;
    this.articlesSuggeres = [];
  }

  ajouterLigne(): void {
    if (!this.ligneEnCours.article || !this.ligneEnCours.quantite) {
      return;
    }
    this.lignes.push({ ...this.ligneEnCours });
    this.ligneEnCours = {};
    this.articleRecherche = '';
  }

  retirerLigne(index: number): void {
    this.lignes.splice(index, 1);
  }

  totalCommande(): number {
    return this.lignes.reduce((somme, l) =>
      somme + Number(l.quantite ?? 0) * Number(l.prixUnitaire ?? 0), 0);
  }

  cancel(): void {
    this.router.navigate([this.estClient ? '/commandeclient' : '/commandefournissuer']);
  }

  enregistrer(): void {
    this.errorMsg = [];
    if (!this.partenaireChoisi) {
      this.errorMsg = [`Veuillez sélectionner un ${this.estClient ? 'client' : 'fournisseur'}.`];
      return;
    }
    if (this.lignes.length === 0) {
      this.errorMsg = ['Ajoutez au moins un article à la commande.'];
      return;
    }

    this.enCours = true;
    const lignesCommunes = this.lignes.map(l => ({
      article: { id: l.article!.id },
      quantite: l.quantite,
      prixUnitaire: l.prixUnitaire
    }));

    if (this.estClient) {
      const commande: CommandeClient = {
        code: 'CMD-' + Date.now(),
        client: { id: this.partenaireChoisi.id },
        ligneComandeClientList: lignesCommunes as LigneCommandeClient[]
      };
      this.commandeClientService.enregistrerCommande(commande).subscribe(
        () => this.cancel(),
        (error: any) => this.traiterErreur(error)
      );
    } else {
      const commande: CommandeFournisseur = {
        code: 'CMDF-' + Date.now(),
        fournisseur: { id: this.partenaireChoisi.id },
        ligneComandeFournisseurList: lignesCommunes as LigneCommandeFournisseur[]
      };
      this.commandeFournisseurService.enregistrerCommande(commande).subscribe(
        () => this.cancel(),
        (error: any) => this.traiterErreur(error)
      );
    }
  }

  private traiterErreur(error: any): void {
    this.enCours = false;
    console.log(error);
    this.errorMsg = error?.error?.errors?.length
      ? error.error.errors
      : [error?.error?.message ?? 'Erreur lors de l\'enregistrement de la commande.'];
  }
}
