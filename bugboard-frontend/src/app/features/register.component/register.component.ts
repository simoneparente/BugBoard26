import { Component, inject, OnInit, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { AuthService } from '../../core/auth/auth-service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FooterComponent } from '../../layout/footer.component/footer.component';
import { BrandLogoComponent } from '../../layout/brand-logo.component/brand-logo.component';
import { NotificationService } from '../../core/services/notification.service';

/**
 * Custom validator to ensure password and confirmPassword fields match.
 */
export function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password && confirmPassword && password !== confirmPassword
    ? { passwordsMismatch: true }
    : null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, BrandLogoComponent, FooterComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  public readonly isSubmitting = signal<boolean>(false);
  public readonly registerError = signal<string | null>(null);
  public readonly showPassword = signal<boolean>(false);

  // Form setup with token disabled to prevent user tampering
  public readonly registerForm = this.formBuilder.nonNullable.group(
    {
      token: [{ value: '', disabled: true }, Validators.required],
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatchValidator },
  );

  ngOnInit(): void {
    // Extract token from HTTP query parameters
    const tokenParam = this.route.snapshot.queryParamMap.get('token');
    if (tokenParam) {
      this.registerForm.controls.token.setValue(tokenParam);
    } else {
      this.highlightErrorField('token');
      this.registerError.set('Invalid or missing registration token.');
    }
  }

  /**
   * @param fieldName name of the target form control
   * @returns true if the form control is invalid and has been touched, false otherwise
   */
  public isFieldInvalid(
    fieldName: 'token' | 'username' | 'email' | 'password' | 'confirmPassword',
  ): boolean {
    const field = this.registerForm.controls[fieldName];
    return !!(field && field.invalid && field.touched);
  }

  /**
   * Checks if the cross-field password match validation failed.
   */
  public hasPasswordMismatch(): boolean {
    const confirmPasswordField = this.registerForm.controls.confirmPassword;
    return !!(confirmPasswordField.touched && this.registerForm.errors?.['passwordsMismatch']);
  }

  public togglePasswordVisibility(): void {
    this.showPassword.update((value) => !value);
  }

  public onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.registerError.set(null);

    const payload = this.registerForm.getRawValue();

    this.authService.register(payload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.notificationService.showSuccess('Success', 'Registration successful! Please log in.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const msg = err.error?.message || 'Registration failed.';
        this.registerError.set(msg);
        this.highlightErrorField(msg);
      },
    });
  }

  public hasServerError(fieldName: string): boolean {
    return !!this.registerForm.get(fieldName)?.hasError('serverError');
  }

  private highlightErrorField(errorMessage: string): void {
    const error = errorMessage.toLowerCase();

    if (error.includes('username')) {
      this.registerForm.controls.username.setErrors({ serverError: true });
      this.registerForm.controls.username.markAsTouched();
    }
    if (error.includes('email')) {
      this.registerForm.controls.email.setErrors({ serverError: true });
      this.registerForm.controls.email.markAsTouched();
    }
    if (error.includes('token')) {
      this.registerForm.controls.token.setErrors({ serverError: true });
      this.registerForm.controls.token.markAsTouched();
    }
  }
}
