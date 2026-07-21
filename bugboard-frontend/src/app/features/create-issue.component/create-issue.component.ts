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
import { TagResponse } from '../../core/tag.model';
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
  tagSearchQuery: string = '';
  selectedFiles: File[] = [];
  projectId: string = '';
  availableTags: TagResponse[] = [];
  selectedTags: TagResponse[] = [];

  constructor() {
    this.issueForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(256)]],
      description: ['', [Validators.required, Validators.maxLength(1000)]],
      priority: ['MEDIUM', Validators.required],
      type: ['BUG', Validators.required],
    });
  }

  ngOnInit() {
    // Only run API calls in the browser to avoid SSR 401 unauthenticated requests during server pre-rendering
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
        } else {
          this.isLoadingTags = false;
        }
      });
    } else {
      this.isLoadingTags = false;
    }
  }

  private loadTags(projectId: string) {
    this.isLoadingTags = true;
    this.tagService.getTagsByProjectId(projectId).subscribe({
      next: (tags) => {
        console.log('Fetched tags for project:', projectId, tags);
        this.availableTags = tags || [];
        this.isLoadingTags = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load tags for project:', projectId, err);
        this.isLoadingTags = false;
        this.cdr.detectChanges();
      },
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
    const clickedInside = this.elementRef.nativeElement
      .querySelector('.tags-dropdown-wrapper')
      ?.contains(event.target);
    if (!clickedInside) {
      this.isTagDropdownOpen = false;
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

  discardDraft() {
    this.issueForm.reset({
      priority: 'MEDIUM',
      type: 'BUG',
    });
    this.selectedFiles = [];
    this.submitted = false;
    this.showError = false;
    this.selectedTags = [];
    this.selectedFiles = [];
    this.tagSearchQuery = '';
  }
}
