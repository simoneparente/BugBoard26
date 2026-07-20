import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { IssueService } from '../../core/services/issue.service';
import { NotificationService } from '../../core/services/notification.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-create-issue',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './create-issue.component.html',
  styleUrls: ['./create-issue.component.scss'],
})
export class CreateIssueComponent {
  private readonly fb = inject(FormBuilder);
  private readonly issueService = inject(IssueService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notificationService = inject(NotificationService);

  issueForm: FormGroup;
  submitted = false;
  showError = false;
  selectedFiles: File[] = [];
  projectId: string = '';

  constructor() {
    this.projectId = this.route.snapshot.paramMap.get('projectId') || '';
    this.issueForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(256)]],
      description: ['', [Validators.required, Validators.maxLength(1000)]],
      priority: ['MEDIUM', Validators.required],
      type: ['BUG', Validators.required],
    });
  }

  get f() {
    return this.issueForm.controls;
  }

  onSubmit() {
    this.submitted = true;
    this.showError = false;

    if (this.issueForm.invalid) {
      this.showError = true;
      return;
    }

    const projectId = this.projectId;
    const payload = {
      ...this.issueForm.value,
    };

    this.issueService.createIssue(projectId, payload).subscribe({
      next: (response) => {
        if (this.selectedFiles.length > 0) {
          const uploadRequests = this.selectedFiles.map((file) =>
            this.issueService.uploadAttachment(response.id, file),
          );
          forkJoin(uploadRequests).subscribe({
            next: () => {
              this.notificationService.showSuccess('Success', 'Issue created with attachments!');
              this.router.navigate(['/projects', projectId, 'issues', response.id]);
            },
            error: () => this.notificationService.showError('Upload Error', 'Failed to upload attachments.'),
          });
        } else {
          this.notificationService.showSuccess('Success', 'Issue created successfully!');
          this.router.navigate(['/projects', projectId, 'issues', response.id]);
        }
      },
      error: () => {
        this.showError = true;
      },
    });
  }

  onFileSelected(event: any) {
    const files = event.target.files;
    if (files) {
      for(let file of files) {
        this.selectedFiles.push(file);
      }
    }
  }

  removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
  }

  discardDraft() {
    this.issueForm.reset({
      priority: 'MEDIUM',
      type: 'BUG',
    });
    this.selectedFiles = [];
    this.submitted = false;
    this.showError = false;
  }
}
