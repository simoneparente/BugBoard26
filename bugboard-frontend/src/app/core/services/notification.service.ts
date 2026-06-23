import { Injectable, signal } from '@angular/core';
import { ToastMessage } from '../toast.model';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  //Signal queue to hold the toast messages
  private toastsSignal = signal<ToastMessage[]>([]);

  readonly toasts = this.toastsSignal.asReadonly();

  private counter = 0;
  //Map to hold active timeouts for each toast message
  private activeTimeouts = new Map<number, ReturnType<typeof setTimeout>>(); 

  show(title: string, message: string, type: ToastMessage['type'] = 'info', duration = 5000) {
    const id = this.counter++;
    const newToast: ToastMessage = { id, title, message, type, duration };

    // Add the new toast to the queue
    this.toastsSignal.update((currentToasts) => [...currentToasts, newToast]);

    // Automatic removal of the toast after the specified duration
    if (duration > 0) {
      const timeoutId = setTimeout(() => {
        this.remove(id);
      }, duration);
      this.activeTimeouts.set(id, timeoutId);
    }
  }

  showSuccess(title: string, message: string, duration?: number) {
    this.show(title, message, 'success', duration);
  }

  showError(title: string, message: string, duration?: number) {
    this.show(title, message, 'error', duration);
  }

  showWarning(title: string, message: string, duration?: number) {
    this.show(title, message, 'warning', duration);
  }

  showInfo(title: string, message: string, duration?: number) {
    this.show(title, message, 'info', duration);
  }

  remove(id: number) {
    // Clear the timeout for the toast if it exists
    if (this.activeTimeouts.has(id)) {
      clearTimeout(this.activeTimeouts.get(id));
      this.activeTimeouts.delete(id);
    }
    this.toastsSignal.update((currentToasts) => currentToasts.filter((toast) => toast.id !== id));
  }

  removeAll() {
    this.activeTimeouts.forEach(timeoutId => clearTimeout(timeoutId));
    this.activeTimeouts.clear();
    this.toastsSignal.set([]);
  }
}
