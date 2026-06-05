import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  readonly ERROR_MESSAGES = {
    notFilled: "Please fill in all required fields.",
    unexpected: "An unexpected error occurred during login.",
    invalidEmail: "Please enter a valid email address.",
    required: "This field is required."
  };
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  errorMessage: string = "";
  isLoading: boolean = false;

  loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  onSubmit(): void {
    if (this.loginForm.invalid){
      this.loginForm.markAllAsTouched();
      this.errorMessage = this.ERROR_MESSAGES.notFilled;
      this.isLoading = false;
      return;
    } 

    this.isLoading = true;
    this.errorMessage = "";

    this.authService.login(this.loginForm.getRawValue())
      .pipe(
        finalize(() => {
          this.isLoading = false;
        })
      )
    .subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err: any) => {
        this.errorMessage = err.error?.message || this.ERROR_MESSAGES.unexpected;
      }
    });
  }
}
