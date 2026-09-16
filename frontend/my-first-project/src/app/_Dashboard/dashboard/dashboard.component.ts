import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Dashboard, MOIS_LABELS } from '../../Models/Dashboard';
import { DashboardService } from '../../services/dashboard.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  data: Dashboard | null = null;
  loading = true;
  erreur = '';
  moisLabel = MOIS_LABELS;
  anneeCourante = new Date().getFullYear();
  utilisateur: any = {};

  constructor(
    private dashboardService: DashboardService,
    private userService: UserService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.utilisateur = this.userService.getConnectedUser();
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.erreur = '';
    this.dashboardService.getDashboard().subscribe({
      next: (data) => {
        this.data = data;
        this.loading = false;
      },
      error: () => {
        this.erreur = 'Impossible de charger les statistiques. Vérifiez que le serveur est démarré.';
        this.loading = false;
      }
    });
  }

  /** Redirige vers la création avec pré-remplissage (articles, clients, etc.) */
  nouvelleVente(): void {
    this.router.navigate(['/commandeclient']);
  }

  nouvelArticle(): void {
    this.router.navigate(['/newarticles']);
  }

  nouveauClient(): void {
    this.router.navigate(['/newclient']);
  }

  formatMoney(v: number | undefined): string {
    const n = Number(v ?? 0);
    return n.toLocaleString('fr-FR', { maximumFractionDigits: 0 }) + ' F';
  }

  /** Transforme [{annee,mois,ca}] en série continue sur 12 mois pour l'année courante */
  serieMensuelle(): number[] {
    const serie = new Array<number>(12).fill(0);
    (this.data?.ventesParMois ?? []).forEach(v => {
      if (v.annee === this.anneeCourante && v.mois >= 1 && v.mois <= 12) {
        serie[v.mois - 1] = Number(v.chiffreAffaires) || 0;
      }
    });
    return serie;
  }

  maxMensuel(): number {
    return Math.max(...this.serieMensuelle(), 1);
  }

  moisMaxLabel(): string {
    const serie = this.serieMensuelle();
    const i = serie.indexOf(Math.max(...serie));
    return this.moisLabel[i] ?? '';
  }

  /** Top catégorie par CA */
  topCategorie(): string {
    const list = [...(this.data?.ventesParCategorie ?? [])].sort(
      (a, b) => Number(b.chiffreAffaires) - Number(a.chiffreAffaires)
    );
    return list[0]?.designation ?? '—';
  }

  /** Meilleur client par montant total */
  topClient(): string {
    const list = [...(this.data?.commandesParClient ?? [])].sort(
      (a, b) => Number(b.montantTotal) - Number(a.montantTotal)
    );
    return list[0] ? `${list[0].prenom} ${list[0].nom}`.trim() : '—';
  }

  /** Largeur de barre en % pour les listes top articles / catégories / clients */
  pct(val: number, list: Array<{ chiffreAffaires?: number; montantTotal?: number }>, field: 'chiffreAffaires' | 'montantTotal'): number {
    const max = Math.max(...list.map(x => Number(x[field]) || 0), 1);
    return Math.max(4, Math.round((Number(val) / max) * 100));
  }

  pourcentageCAJour(): number {
    const mois = Number(this.data?.chiffreAffairesMoisEnCours) || 0;
    const jour = Number(this.data?.chiffreAffairesJourEnCours) || 0;
    return mois > 0 ? Math.min(100, Math.round((jour / mois) * 100)) : 0;
  }

  initiales(nom: string, prenom: string): string {
    return ((prenom?.[0] ?? '') + (nom?.[0] ?? '')).toUpperCase() || '?';
  }
}
