import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ProjectService } from '../../core/services/project.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-create-project',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './create-project.component.html',
  styleUrl: './create-project.component.scss',
})
export class CreateProjectComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly notificationService = inject(NotificationService);

  public isSubmitting = signal<boolean>(false);
  public error = signal<string | null>(null);

  public projectForm = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.maxLength(500)]],
  });

  public isFieldInvalid(field: 'title' | 'description'): boolean {
    const control = this.projectForm.controls[field];
    return control.invalid && (control.dirty || control.touched);
  }

  public get descriptionLength(): number {
    return this.projectForm.controls.description.value.length;
  }

  public get titleLength(): number {
    return this.projectForm.controls.title.value.length;
  }

  public onSubmit(): void {
    if (this.projectForm.invalid) {
      this.projectForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.error.set(null);

    const { title, description } = this.projectForm.getRawValue();

    this.projectService.create(title, description).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigate(['/projects']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.error.set(err?.error?.message || 'Failed to create project. Please try again.');
      },
    });
  }

  public cancel(): void {
    this.router.navigate(['/projects']);
  }
}
