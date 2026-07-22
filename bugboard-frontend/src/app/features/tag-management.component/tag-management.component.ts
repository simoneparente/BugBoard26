import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TagService } from '../../core/services/tag.service';
import { NotificationService } from '../../core/services/notification.service';
import { TagRequest, TagResponse } from '../../core/tag.model';

@Component({
  selector: 'app-tag-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './tag-management.component.html',
  styleUrl: './tag-management.component.scss',
})
export class TagManagementComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly tagService = inject(TagService);
  private readonly notificationService = inject(NotificationService);

  // Route parameter
  private readonly projectId = signal<string | null>(null);

  // State
  public readonly tags = signal<TagResponse[]>([]);
  public readonly loading = signal(true);
  public readonly error = signal<string | null>(null);
  public readonly isSubmitting = signal(false);
  public readonly editingTagId = signal<string | null>(null);

  // Form
  public readonly tagForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    color: ['#6366f1', [Validators.required, Validators.pattern(/^#[0-9A-Fa-f]{6}$/)]],
  });

  // Derived values
  public readonly isEditMode = computed(() => this.editingTagId() !== null);
  public readonly formTitle = computed(() => (this.isEditMode() ? 'Edit Tag' : 'Create Tag'));
  public readonly submitButtonText = computed(() =>
    this.isEditMode() ? 'Update Tag' : 'Create Tag',
  );

  ngOnInit(): void {
    this.loadProjectId();
    this.loadTags();
    this.resetForm();
  }

  /**
   * Extract projectId from route parameters.
   */
  private loadProjectId(): void {
    this.activatedRoute.paramMap.subscribe((params) => {
      const id = params.get('projectId');
      this.projectId.set(id);
    });
  }

  /**
   * Load tags for the project.
   */
  private loadTags(): void {
    const projectId = this.projectId();
    if (!projectId) {
      this.error.set('Project ID not found in route');
      this.loading.set(false);
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.tagService.getTagsByProjectId(projectId).subscribe({
      next: (data) => {
        this.tags.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load tags:', err);
        this.error.set('Failed to load tags');
        this.loading.set(false);
      },
    });
  }

  /**
   * Submit the form to create or update a tag.
   */
  public onSubmit(): void {
    if (this.tagForm.invalid || !this.projectId()) {
      this.tagForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    const request: TagRequest = {
      name: this.tagForm.value.name!,
      color: this.tagForm.value.color!,
      projectId: this.projectId()!,
    };

    if (this.isEditMode()) {
      this.performUpdate(request);
    } else {
      this.performCreate(request);
    }
  }

  /**
   * Perform create operation.
   */
  private performCreate(request: TagRequest): void {
    this.tagService.createTag(request).subscribe({
      next: (newTag) => {
        this.tags.update((tags) => [...tags, newTag]);
        this.tagForm.reset({ color: this.generateRandomColor() });
        this.isSubmitting.set(false);
        this.notificationService.showSuccess('Success', 'Tag created successfully');
      },
      error: (err) => {
        console.error('Failed to create tag:', err);
        const message = err.error?.message || 'Failed to create tag';
        this.notificationService.showError('Error', message);
        this.isSubmitting.set(false);
      },
    });
  }

  /**
   * Perform update operation.
   */
  private performUpdate(request: TagRequest): void {
    const tagId = this.editingTagId();
    if (!tagId) return;

    this.tagService.updateTag(tagId, request).subscribe({
      next: (updatedTag) => {
        this.tags.update((tags) => tags.map((tag) => (tag.id === tagId ? updatedTag : tag)));
        this.resetForm();
        this.isSubmitting.set(false);
        this.notificationService.showSuccess('Success', 'Tag updated successfully');
      },
      error: (err) => {
        console.error('Failed to update tag:', err);
        const message = err.error?.message || 'Failed to update tag';
        this.notificationService.showError('Error', message);
        this.isSubmitting.set(false);
      },
    });
  }

  /**
   * Start editing a tag.
   */
  public onEditTag(tag: TagResponse): void {
    this.editingTagId.set(tag.id);
    this.tagForm.patchValue({
      name: tag.name,
      color: tag.color,
    });
    // Scroll to form for UX
    setTimeout(() => {
      document.querySelector('.tag-form')?.scrollIntoView({ behavior: 'smooth' });
    }, 0);
  }

  /**
   * Cancel editing and reset form.
   */
  public onCancelEdit(): void {
    this.resetForm();
  }

  /**
   * Delete a tag with confirmation.
   */
  public onDeleteTag(tag: TagResponse): void {
    const confirmed = confirm(`Are you sure you want to delete the tag "${tag.name}"?`);
    if (!confirmed) return;

    this.tagService.deleteTag(tag.id).subscribe({
      next: () => {
        this.tags.update((tags) => tags.filter((t) => t.id !== tag.id));
        this.notificationService.showSuccess('Success', 'Tag deleted successfully');
      },
      error: (err) => {
        console.error('Failed to delete tag:', err);
        const message = err.error?.message || 'Failed to delete tag';
        this.notificationService.showError('Error', message);
      },
    });
  }

  /**
   * Check if a field is invalid and should show error.
   */
  public isFieldInvalid(fieldName: 'name' | 'color'): boolean {
    const control = this.tagForm.get(fieldName);
    return control ? control.invalid && (control.dirty || control.touched) : false;
  }

  /**
   * Reset form to initial state.
   */
  private resetForm(): void {
    this.tagForm.reset({ color: this.generateRandomColor() });
    this.editingTagId.set(null);
  }

  private generateRandomColor(): string {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
  }
}
