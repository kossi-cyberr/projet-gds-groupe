import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Category } from 'src/app/Models/Category';
import { CategoryService } from 'src/app/services/category.service';
import { ColonneTable } from 'src/app/_Dashboard/components/data-table/data-table.component';

interface ActionTable {
  type: 'details' | 'modifier' | 'supprimer';
  ligne: Category;
}

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css']
})
export class CategoriesComponent implements OnInit {

  listCategory: Category[] = [];
  chargement = true;
  errorMsg = '';

  colonnes: ColonneTable<Category>[] = [
    { cle: 'codeCategory', libelle: 'col.code', triable: true },
    { cle: 'designation', libelle: 'col.designation', triable: true },
    { cle: 'actions', libelle: 'col.actions', actions: true }
  ];

  /** État de la modale de suppression. */
  suppressionOuverte = false;
  messageSuppression = '';
  private idSuppression: number | undefined;

  constructor(private router: Router,
    private categoryService: CategoryService) { }

  ngOnInit(): void {
    this.findAllCategories();
  }

  findAllCategories(): void {
    this.chargement = true;
    this.errorMsg = '';
    this.categoryService.findAll()
      .subscribe((res: Category[]) => {
        this.listCategory = res;
        this.chargement = false;
      }, () => {
        this.errorMsg = 'Erreur lors du chargement des catégories.';
        this.chargement = false;
      });
  }

  nouvelleCategorie(): void {
    this.router.navigate(['newcategorie']);
  }

  /** Ouvre la confirmation de suppression pour une catégorie. */
  surAction(action: ActionTable): void {
    const category = action.ligne;
    if (action.type === 'modifier' || action.type === 'details') {
      this.router.navigate(['newcategorie', category.id]);
      return;
    }
    if (action.type === 'supprimer') {
      this.idSuppression = category.id;
      this.messageSuppression =
        `Êtes-vous sûr de vouloir supprimer la catégorie « ${category.codeCategory ?? ''} » ? Cette action est irréversible.`;
      this.suppressionOuverte = true;
    }
  }

  confirmerEtSupprimer(): void {
    if (!this.idSuppression) {
      return;
    }
    this.categoryService.deleteCategory(this.idSuppression).subscribe(
      () => {
        this.fermerSuppression();
        this.findAllCategories();
      },
      (error) => {
        this.fermerSuppression();
        // Message métier du backend (catégorie utilisée par des articles…)
        this.errorMsg = error?.error?.message ?? 'Suppression impossible.';
        setTimeout(() => { this.errorMsg = ''; }, 5000);
      });
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
