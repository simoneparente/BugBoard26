import { Component, inject } from '@angular/core';
import { NotificationService } from '../../core/services/notification.service';
import { ToastMessage } from '../../core/toast.model';

const TOAST_UI_CONFIG: Record<ToastMessage['type'], { icon: string; color: string }> = {
  success: { icon: 'bi-check-circle', color: 'text-success' },
  error: { icon: 'bi-x-circle', color: 'text-danger' },
  warning: { icon: 'bi-exclamation-triangle-fill', color: 'text-warning' },
  info: { icon: 'bi-info-circle', color: 'text-primary' },
};

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [],
  templateUrl: './toast-container.component.html',
  styleUrl: './toast-container.component.scss',
})
export class ToastContainerComponent {
  protected readonly notificationService = inject(NotificationService);

  protected readonly uiConfig = TOAST_UI_CONFIG;

  getIconClass(type: ToastMessage['type']): string {
    switch (type) {
      case 'success': return 'bi bi-check-circle';
      case 'error': return 'bi bi-x-circle';
      case 'warning': return 'bi bi-exclamation-triangle-fill';
      case 'info':
      default: return 'bi bi-info-circle';
    }
  }

  getColorClass(type: ToastMessage['type']): string {
    switch (type) {
      case 'success': return 'text-success';
      case 'error': return 'text-danger';
      case 'warning': return 'text-warning';
      case 'info':
      default: return 'text-primary';
    }
  }
}
