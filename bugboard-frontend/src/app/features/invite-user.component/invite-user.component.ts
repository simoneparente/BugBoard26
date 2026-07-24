import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { InvitationResponse } from '../../core/invitation.model';
import { ROLES } from '../../core/roles.model';
import { NotificationService } from '../../core/services/notification.service';
import { ApiService } from '../../core/services/api.service';

export interface RoleOption {
  key: keyof typeof ROLES;
  title: string;
  badge: string;
  icon: string;
  description: string;
  accentClass: string;
}

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
  generatedRole = signal<keyof typeof ROLES | null>(null);
  isCopied = signal<boolean>(false);
  isFlashing = signal<boolean>(false);

  // Form Control for role (Default: TECHNICAL)
  roleControl = new FormControl<keyof typeof ROLES>('TECHNICAL', Validators.required);

  readonly rolesList: RoleOption[] = [
    {
      key: ROLES.TECHNICAL,
      title: 'TECHNICAL',
      badge: 'TECHNICAL',
      icon: 'bi-code-slash',
      description: 'Can manage issues, work on tasks, and create project reports.',
      accentClass: 'accent-primary',
    },
    {
      key: ROLES.ADMIN,
      title: 'ADMIN',
      badge: 'ADMIN',
      icon: 'bi-shield-lock-fill',
      description: 'Full workspace control, member invitations, and global settings.',
      accentClass: 'accent-purple',
    },
    {
      key: ROLES.EXTERNAL,
      title: 'EXTERNAL',
      badge: 'EXTERNAL',
      icon: 'bi-person-badge-fill',
      description: 'Restricted access for external partners and guest reporters.',
      accentClass: 'accent-amber',
    },
  ];

  selectRole(roleKey: keyof typeof ROLES) {
    if (this.roleControl.value !== roleKey) {
      this.roleControl.setValue(roleKey);
      this.roleControl.markAsTouched();
      // Reset generated link when switching roles to prevent ambiguity
      this.generatedLink.set(null);
      this.generatedRole.set(null);
    }
  }

  generateLink() {
    if (this.roleControl.invalid || this.isLoading()) {
      this.notificationService.showWarning('Warning', 'Please select a valid role.');
      return;
    }

    const selectedRole = this.roleControl.value as ROLES;
    this.isLoading.set(true);
    const payload = { role: selectedRole };

    this.apiService.invitations.create(payload).subscribe({
      next: (response) => {
        const frontendUrl = window.location.origin;
        const fullInviteLink = `${frontendUrl}/register?token=${response.token}`;
        this.generatedLink.set(fullInviteLink);
        this.generatedRole.set(selectedRole);
        this.isLoading.set(false);
        this.triggerFlash();
        this.notificationService.removeAll(); // Clear any previous toasts
        this.notificationService.showSuccess('Success', `Link generated for ${selectedRole}!`);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.notificationService.showError('error', 'Error generating link. Please try again.');
      },
    });
  }

  private triggerFlash() {
    this.isFlashing.set(true);
    setTimeout(() => this.isFlashing.set(false), 800);
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
    this.generatedRole.set(null);
    this.roleControl.setValue('TECHNICAL');
    this.isCopied.set(false);
    this.isFlashing.set(false);
  }
}
