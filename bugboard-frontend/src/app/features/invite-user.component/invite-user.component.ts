import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { InvitationResponse } from '../../core/invitation.model';
import { ROLES } from '../../core/roles.model';
import { NotificationService } from '../../core/services/notification.service';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-invite-user',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './invite-user.component.html',
  styleUrl: './invite-user.component.scss',
})
export class InviteUserComponent {
  private readonly apiService = inject(ApiService);
  private readonly notificationService = inject(NotificationService);
  ROLES = ROLES;

  isLoading = signal<boolean>(false);
  generatedLink = signal<string | null>(null);
  isCopied = signal<boolean>(false);

  isCooldown = signal<boolean>(false);
  cooldownTime = signal<number>(0);
  private cooldownInterval: any; //Ref to the timer

  // Form Control for role (Default: TECHNICAL)
  roleControl = new FormControl<keyof typeof ROLES>('TECHNICAL', Validators.required);

  generateLink() {
    this.notificationService.showInfo('Info', 'Generating link...', 2000);
    if (this.roleControl.invalid || this.isCooldown() || this.isLoading()) {
      this.notificationService.showWarning('Warning', 'Please select a valid role.');
      return;
    }

    this.isLoading.set(true);
    const payload = { role: this.roleControl.value as ROLES };

    this.apiService.invitations.create(payload).subscribe({
      next: (response) => {
        const frontendUrl = window.location.origin;
        const fullInviteLink = `${frontendUrl}/register?token=${response.token}`;
        this.generatedLink.set(fullInviteLink);
        this.isLoading.set(false);
        this.notificationService.removeAll(); // Clear any previous toasts
        this.notificationService.showSuccess('Success', 'Link generated successfully!');
        this.startCooldown();
      },
      error: (err) => {
        this.isLoading.set(false);
        this.notificationService.showError('error', 'Error generating link. Please try again.');
      },
    });
  }
  private startCooldown() {
    this.isCooldown.set(true);
    this.cooldownTime.set(5);

    this.cooldownInterval = setInterval(() => {
      this.cooldownTime.update((time) => time - 1);

      if (this.cooldownTime() <= 0) {
        this.clearCooldown();
      }
    }, 1000);
  }

  private clearCooldown() {
    if (this.cooldownInterval) {
      clearInterval(this.cooldownInterval);
    }
    this.isCooldown.set(false);
    this.cooldownTime.set(0);
  }

  copyToClipboard(link: string) {
    navigator.clipboard.writeText(link).then(() => {
      this.isCopied.set(true);
      this.notificationService.showInfo('Info', 'Link copied to clipboard');
      setTimeout(() => this.isCopied.set(false), 2000);
    });
  }

  resetPanel() {
    this.generatedLink.set(null);
    this.roleControl.setValue('TECHNICAL');
    this.isCopied.set(false);
    this.clearCooldown();
  }

  ngOnDestroy() {
    this.clearCooldown();
  }
}
