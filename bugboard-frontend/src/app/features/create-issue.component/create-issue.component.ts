import {
  Component,
  OnInit,
  ChangeDetectorRef,
  ElementRef,
  HostListener,
  inject,
  PLATFORM_ID,
} from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { IssueService } from '../../core/services/issue.service';
import { NotificationService } from '../../core/services/notification.service';
import { TagService } from '../../core/services/tag.service';
import { ProjectService } from '../../core/services/project.service';
import { BreadcrumbService } from '../../core/services/breadcrumb.service';
import { TagResponse } from '../../core/tag.model';
import { ProjectResponse } from '../../core/project.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-create-issue',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule],
  templateUrl: './create-issue.component.html',
  styleUrls: ['./create-issue.component.scss'],
})
export class CreateIssueComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly issueService = inject(IssueService);
  private readonly tagService = inject(TagService);
  private readonly projectService = inject(ProjectService);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly notificationService = inject(NotificationService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly elementRef = inject(ElementRef);

  issueForm: FormGroup;
  submitted = false;
  showError = false;
  isLoadingTags = true;

  isTagDropdownOpen = false;
  isPriorityDropdownOpen = false;
  isTypeDropdownOpen = false;

  tagSearchQuery: string = '';

  selectedFiles: File[] = [];
  projectId: string = '';
  availableTags: TagResponse[] = [];
  selectedTags: TagResponse[] = [];

  priorities = [
    { value: 'LOWEST', label: 'Lowest', class: 'priority-lowest' },
    { value: 'LOW', label: 'Low', class: 'priority-low' },
    { value: 'MEDIUM', label: 'Medium', class: 'priority-medium' },
    { value: 'HIGH', label: 'High', class: 'priority-high' },
    { value: 'HIGHEST', label: 'Highest', class: 'priority-highest' },
  ];

  types = [
    { value: 'BUG', label: 'Bug', icon: 'bi-bug-fill text-danger' },
    {
      value: 'FEATURE',
      label: 'Feature',
      icon: 'bi-star-fill text-primary',
    },
    {
      value: 'QUESTION',
      label: 'Question',
      icon: 'bi-question-circle-fill text-warning',
    },
    {
      value: 'DOCUMENTATION',
      label: 'Documentation',
      icon: 'bi-file-earmark-text-fill text-info',
    },
    { value: 'OTHER', label: 'Other', icon: 'bi-tag-fill text-secondary' },
  ];

  constructor() {
    this.issueForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(256)]],
      description: ['', [Validators.required, Validators.maxLength(1000)]],
      priority: ['MEDIUM', Validators.required],
      type: ['BUG', Validators.required],
    });
  }

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.route.paramMap.subscribe((params) => {
        let id = params.get('projectId') || this.route.parent?.snapshot.paramMap.get('projectId');

        if (!id && typeof window !== 'undefined') {
          const match = window.location.pathname.match(/projects\/([a-f0-9-]+)/i);
          if (match) {
            id = match[1];
          }
        }

        this.projectId = id || '';
        if (this.projectId) {
          this.loadTags(this.projectId);
          this.loadProjectDetails(this.projectId);
        } else {
          this.isLoadingTags = false;
        }
      });
    } else {
      this.isLoadingTags = false;
    }
  }

  private loadProjectDetails(projectId: string) {
    this.projectService.getById(projectId).subscribe({
      next: (project: ProjectResponse) => {
        if (project?.name) {
          this.breadcrumbService.setProjectName(project.name);
        }
      },
      error: (err: any) => {
        console.error('Failed to load project details for breadcrumbs:', err);
      },
    });
  }

  private loadTags(projectId: string) {
    this.isLoadingTags = true;
    this.tagService.getTagsByProjectId(projectId).subscribe({
      next: (tags) => {
        this.availableTags = tags || [];
        this.isLoadingTags = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.notificationService.showError('Error', 'Failed to load tags for the project.');
        this.isLoadingTags = false;
        this.cdr.detectChanges();
      },
    });
  }

  get f() {
    return this.issueForm.controls;
  }

  get currentPriorityObj() {
    const current = this.issueForm.get('priority')?.value || 'MEDIUM';
    return this.priorities.find((p) => p.value === current) || this.priorities[2];
  }

  get currentTypeObj() {
    const current = this.issueForm.get('type')?.value || 'BUG';
    return this.types.find((t) => t.value === current) || this.types[0];
  }

  selectPriority(value: string) {
    this.issueForm.patchValue({ priority: value });
    this.isPriorityDropdownOpen = false;
  }

  selectType(value: string) {
    this.issueForm.patchValue({ type: value });
    this.isTypeDropdownOpen = false;
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
      tags: this.selectedTags,
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
            error: () =>
              this.notificationService.showError('Upload Error', 'Failed to upload attachments.'),
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
      for (let file of files) {
        this.selectedFiles.push(file);
      }
    }
  }

  removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
  }

  get filteredAvailableTags(): TagResponse[] {
    const query = (this.tagSearchQuery || '').trim().toLowerCase();
    return (this.availableTags || []).filter(
      (tag) => !this.isTagSelected(tag) && (query === '' || tag.name.toLowerCase().includes(query)),
    );
  }

  isTagSelected(tag: TagResponse): boolean {
    return this.selectedTags.some((t) => t.id === tag.id);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const nativeEl = this.elementRef.nativeElement;

    if (!nativeEl.querySelector('.tags-dropdown-wrapper')?.contains(event.target)) {
      this.isTagDropdownOpen = false;
    }
    if (!nativeEl.querySelector('.priority-dropdown-wrapper')?.contains(event.target)) {
      this.isPriorityDropdownOpen = false;
    }
    if (!nativeEl.querySelector('.type-dropdown-wrapper')?.contains(event.target)) {
      this.isTypeDropdownOpen = false;
    }
  }

  selectTag(tag: TagResponse) {
    if (!this.isTagSelected(tag)) {
      this.selectedTags.push(tag);
    }
  }

  removeTag(tag: TagResponse) {
    const index = this.selectedTags.findIndex((t) => t.id === tag.id);
    if (index > -1) {
      this.selectedTags.splice(index, 1);
    }
  }
}
