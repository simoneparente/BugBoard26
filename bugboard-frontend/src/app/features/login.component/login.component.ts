import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth/auth-service';
import { Router } from '@angular/router';
import {} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FooterComponent } from '../../layout/footer.component/footer.component';
import { BrandLogoComponent } from '../../layout/brand-logo.component/brand-logo.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrandLogoComponent, FooterComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  public readonly isSubmitting = signal<boolean>(false);
  public readonly loginError = signal<string | null>(null);
  public readonly showPassword = signal<boolean>(false);

  public readonly loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  /**
   *
   * @param fieldName name of the target form control
   * @returns true if the form control is invalid and has been touched or is dirty, false otherwise
   */
  public isFieldInvalid(fieldName: 'email' | 'password'): boolean {
    const field = this.loginForm.controls[fieldName];

    return !!(field && field.invalid && field.touched);
  }

  public togglePasswordVisibility(): void {
    this.showPassword.update((value) => !value);
    return;
  }

  public onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.loginError.set(null);

    const payload = this.loginForm.getRawValue();

    this.authService.login(payload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        if (err.status === 401) {
          this.loginError.set('Credentials do not match our records. Please try again.');
        } else {
          this.loginError.set('An unexpected error occurred. Please try again later.');
        }
      },
    });
  }
}
