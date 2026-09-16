import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

/**
 * Modale de confirmation réutilisable pour les actions destructives
 * (suppression, désactivation…). Ouverte via l'input `ouvert`.
 * Fermeture : bouton Annuler, clic sur l'arrière-plan ou touche Escape.
 */
@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent {
  /** Modale ouverte ? */
  @Input() ouvert = false;
  /** Titre de la modale. */
  @Input() titre = 'Confirmation';
  /** Message affiché (contexte de l'action). */
  @Input() message = 'Êtes-vous sûr de vouloir effectuer cette action ?';
  /** Libellé du bouton de confirmation. */
  @Input() libelleConfirmer = 'Supprimer';
  /** Nom Lucide de l'icône d'illustration. */
  @Input() icone = 'alert-triangle';

  /** Émis à la confirmation (l'hôte ferme la modale). */
  @Output() confirme = new EventEmitter<void>();
  /** Émis à l'annulation (clic extérieur, Annuler, Escape). */
  @Output() annule = new EventEmitter<void>();

  /** Escape ferme la modale (uniquement si elle est ouverte). */
  @HostListener('document:keydown.escape')
  surEscape(): void {
    if (this.ouvert) {
      this.annule.emit();
    }
  }

  confirmer(): void {
    this.confirme.emit();
  }

  annuler(): void {
    this.annule.emit();
  }
}
