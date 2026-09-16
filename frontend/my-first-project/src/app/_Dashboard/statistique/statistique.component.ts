import { Component, OnInit } from '@angular/core';
import { Dashboard, MOIS_LABELS } from '../../Models/Dashboard';
import { DashboardService } from '../../services/dashboard.service';

@Component({
  selector: 'app-statistique',
  templateUrl: './statistique.component.html',
  styleUrls: ['./statistique.component.css']
})
export class StatistiqueComponent implements OnInit {

  data: Dashboard | null = null;
  loading = true;
  erreur = '';
  moisLabel = MOIS_LABELS;
  anneeCourante = new Date().getFullYear();
  couleurs = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796', '#5a5c69', '#fd7e14'];

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
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
        this.erreur = 'Impossible de charger les statistiques.';
        this.loading = false;
      }
    });
  }

  serieMensuelle(): number[] {
    const serie = new Array<number>(12).fill(0);
    const annee = new Date().getFullYear();
    (this.data?.ventesParMois ?? []).forEach(v => {
      if (v.annee === annee && v.mois >= 1 && v.mois <= 12) {
        serie[v.mois - 1] = Number(v.chiffreAffaires) || 0;
      }
    });
    return serie;
  }

  maxMensuel(): number {
    return Math.max(...this.serieMensuelle(), 1);
  }

  caAnnuel(): number {
    return this.serieMensuelle().reduce((a, b) => a + b, 0);
  }

  /** Totaux pour la répartition catégories */
  totalCategories(): number {
    return (this.data?.ventesParCategorie ?? [])
      .reduce((sum, c) => sum + (Number(c.chiffreAffaires) || 0), 0);
  }

  /** Convertit la liste catégories en segments conic-gradient (camembert CSS) */
  segmentsCamembert(): string[] {
    const total = this.totalCategories() || 1;
    let cumul = 0;
    return (this.data?.ventesParCategorie ?? []).map(c => {
      const debut = (cumul / total) * 360;
      cumul += Number(c.chiffreAffaires) || 0;
      const fin = (cumul / total) * 360;
      return `${debut}deg ${fin}deg`;
    });
  }

  /** Couleur de fond complète du camembert (conic-gradient avec les couleurs de la légende) */
  backgroundCamembert(): string {
    const segments = this.segmentsCamembert();
    const parts = segments.map((seg, i) => `${this.nomCouleur(i)} ${seg}`);
    return `conic-gradient(${parts.join(', ')})`;
  }

  pctCategorie(ca: number): number {
    const total = this.totalCategories();
    return total > 0 ? Math.round((Number(ca) / total) * 100) : 0;
  }

  maxClients(): number {
    return Math.max(...(this.data?.commandesParClient ?? []).map(c => Number(c.montantTotal) || 0), 1);
  }

  maxTopArticles(): number {
    return Math.max(...(this.data?.topArticles ?? []).map(a => Number(a.chiffreAffaires) || 0), 1);
  }

  formatMoney(v: number | undefined): string {
    const n = Number(v ?? 0);
    return n.toLocaleString('fr-FR', { maximumFractionDigits: 0 }) + ' F';
  }

  nomCategorie(i: number): string {
    return this.data?.ventesParCategorie?.[i]?.designation ?? '';
  }

  nomCouleur(i: number): string {
    return this.couleurs[i % this.couleurs.length];
  }
}
