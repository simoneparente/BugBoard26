import {
  Component,
  Input,
  Output,
  EventEmitter,
  inject,
  OnInit,
  signal,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormArray,
  FormControl,
  FormBuilder,
  AbstractControl,
} from '@angular/forms';
import { ProjectService } from '../../../core/services/project.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/auth/auth-service';
import { UserResponse } from '../../../core/auth/auth.models';
import { Page } from '../../../core/page.model';

@Component({
  selector: 'app-add-project-members',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-project-members.component.html',
  styleUrl: './add-project-members.component.scss',
})
export class AddProjectMembersComponent implements OnInit {
  @Input() projectId!: string;
  @Output() membersAdded = new EventEmitter<UserResponse[]>();

  private readonly projectService = inject(ProjectService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  // State signals
  availableUsers = signal<UserResponse[]>([]);
  isLoadingUsers = signal<boolean>(false);
  isSubmitting = signal<boolean>(false);
  error = signal<string | null>(null);
  showDropdown = signal<boolean>(false);

  // Authorization
  isAdmin = computed(() => this.authService.userRole() === 'ADMIN'); // ←

  // Form
  membersForm!: FormArray<FormControl<boolean>>;

  ngOnInit() {
    this.initForm();
    this.loadAvailableUsers();
  }

  private initForm(): void {
    this.membersForm = this.fb.array<FormControl<boolean>>([]);
  }

  private loadAvailableUsers(): void {
    this.isLoadingUsers.set(true);
    this.error.set(null);

    this.projectService.getAvailableUsers(this.projectId, 0, 100).subscribe({
      next: (response: Page<UserResponse>) => {
        this.availableUsers.set(response.content ?? []);
        this.updateFormControls();
        this.isLoadingUsers.set(false);
      },
      error: (err) => {
        this.isLoadingUsers.set(false);
        this.error.set('Failed to load available users');
        this.notificationService.showError('Error', 'Could not load available users');
      },
    });
  }

  private updateFormControls(): void {
    this.membersForm.clear();
    this.availableUsers().forEach(() => {
      this.membersForm.push(new FormControl<boolean>(false, { nonNullable: true }));
    });
  }

  getSelectedUserIds(): string[] {
    return this.availableUsers()
      .filter((_, index) => (this.membersForm.at(index) as AbstractControl<boolean>)?.value)
      .map((user) => user.id);
  }

  get selectedCount(): number {
    return this.getSelectedUserIds().length;
  }

  addMembers(): void {
    const selectedIds = this.getSelectedUserIds();

    if (selectedIds.length === 0) {
      this.notificationService.showWarning('Warning', 'Please select at least one user');
      return;
    }

    this.isSubmitting.set(true);
    this.error.set(null);

    this.projectService.addMembersToProject(this.projectId, selectedIds).subscribe({
      next: (addedMembers: UserResponse[]) => {
        this.isSubmitting.set(false);
        this.notificationService.showSuccess(
          'Success',
          `Added ${addedMembers.length} member(s) to the project`,
        );
        this.membersAdded.emit(addedMembers);
        this.resetForm();
        this.loadAvailableUsers();
        this.showDropdown.set(false);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const errorMessage = err.error?.message || 'Failed to add members';
        this.error.set(errorMessage);
        this.notificationService.showError('Error', errorMessage);
      },
    });
  }

  private resetForm(): void {
    this.membersForm.reset();
  }

  toggleDropdown(): void {
    this.showDropdown.update((value) => !value);
  }

  closeDropdown(): void {
    this.showDropdown.set(false);
  }
}
