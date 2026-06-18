import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { InvitationResponse } from '../../core/invitation.model';

@Component({
  selector: 'app-invite-user',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './invite-user.component.html',
  styleUrl: './invite-user.component.scss',
})
export class InviteUserComponent {
  private API_URL = "http://localhost:8080/api/invitations";
  private http = inject(HttpClient);

  isLoading = signal<boolean>(false);
  generatedLink = signal<string | null>(null);
  isCopied = signal<boolean>(false);

  // Form Control for role (Default: TECHNICAL)
  roleControl = new FormControl<'TECHNICAL' | 'ADMIN' | 'EXTERNAL'>('TECHNICAL', Validators.required);

  generateLink() {
    console.log('Generating link for role:', this.roleControl.value);
    if (this.roleControl.invalid){
      console.error('Invalid role selected:', this.roleControl.value);
      return;
    } 

    this.isLoading.set(true);
    const payload = { role: this.roleControl.value };

    console.log('Sending payload to API:', payload);
    this.http.post<InvitationResponse>(`${this.API_URL}`, payload)
      .subscribe({
        next: (response) => {
          console.log('Received response from API:', response);
          const frontendUrl = window.location.origin;
          const fullInviteLink = `${frontendUrl}/register?token=${response.token}`;
          this.generatedLink.set(fullInviteLink);
          this.isLoading.set(false);
        },
        error: (err) => {
          console.error('Errore nella generazione del link', err);
          this.isLoading.set(false);
          //TODO: Show error message to user
        }
      });
  }

  copyToClipboard(link: string) {
    navigator.clipboard.writeText(link).then(() => {
      this.isCopied.set(true);
      setTimeout(() => this.isCopied.set(false), 2000);
    });
  }

  resetPanel() {
    this.generatedLink.set(null);
    this.roleControl.setValue('TECHNICAL');
    this.isCopied.set(false);
  }
}