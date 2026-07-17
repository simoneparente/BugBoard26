import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { IssueService } from '../../core/services/issue.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-issue-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './issue-create.component.html',
  styleUrls: ['./issue-create.component.scss']
})
export class IssueCreateComponent {
  private fb = inject(FormBuilder);
  private issueService = inject(IssueService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  issueForm: FormGroup;
  submitted = false;
  showError = false;
  selectedFiles: File[] = [];
  projectId: string = '';

  constructor() {
    this.projectId = this.route.snapshot.paramMap.get('projectId') || '';
    this.issueForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      priority: ['MEDIUM', Validators.required],
      type: ['BUG', Validators.required]
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
      projectId
    };

    this.issueService.createIssue(projectId, payload).subscribe({
      next: (response) => {
        if (this.selectedFiles.length > 0) {
          const uploadRequests = this.selectedFiles.map(file => this.issueService.uploadAttachment(response.id, file));
          forkJoin(uploadRequests).subscribe({
            next: () => this.router.navigate(['/dashboard']),
            error: () => this.showError = true
          });
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: () => {
        this.showError = true;
      }
    });
  }

  onFileSelected(event: any) {
    const files = event.target.files;
    if (files) {
      for (let i = 0; i < files.length; i++) {
        this.selectedFiles.push(files[i]);
      }
    }
  }

  removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
  }

  discardDraft() {
    this.issueForm.reset({
      priority: 'MEDIUM',
      type: 'BUG'
    });
    this.submitted = false;
    this.showError = false;
  }
}
