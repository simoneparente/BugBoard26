import { Component, inject } from '@angular/core';
import { ConfirmationModalService } from '../../../core/services/confirmation-modal.service';

@Component({
  selector: 'app-confirmation-modal',
  standalone: true,
  imports: [],
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./confirmation-modal.component.scss'],
})
export class ConfirmationModalComponent {
  public readonly confirmService = inject(ConfirmationModalService);

  public readonly state = this.confirmService.modalState;

  /**
   * Close modal if overlay is clicked (standard UX).
   */
  public onOverlayClick(): void {
    this.confirmService.performCancel();
  }
}
