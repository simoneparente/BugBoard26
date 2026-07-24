import { Injectable, signal } from '@angular/core';

export interface ConfirmationModalState {
  isOpen: boolean;
  title: string;
  message: string;
  confirmButtonText: string;
  cancelButtonText: string;
  isDangerous?: boolean;
  onConfirm: (() => void) | null;
  onCancel: (() => void) | null;
}

@Injectable({ providedIn: 'root' })
export class ConfirmationModalService {
  private readonly initialState: ConfirmationModalState = {
    isOpen: false,
    title: '',
    message: '',
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    isDangerous: false,
    onConfirm: null,
    onCancel: null,
  };

  public readonly modalState = signal<ConfirmationModalState>(this.initialState);

  /**
   * Open the modal with custom configuration.
   */
  public open(config: {
    title: string;
    message: string;
    confirmButtonText?: string;
    cancelButtonText?: string;
    isDangerous?: boolean;
    onConfirm: () => void;
    onCancel?: () => void;
  }): void {
    this.modalState.set({
      isOpen: true,
      title: config.title,
      message: config.message,
      confirmButtonText: config.confirmButtonText ?? 'Confirm',
      cancelButtonText: config.cancelButtonText ?? 'Cancel',
      isDangerous: config.isDangerous ?? false,
      onConfirm: config.onConfirm,
      onCancel: config.onCancel ?? null,
    });
  }

  /**
   * Close the modal (reset to initial state).
   */
  public close(): void {
    this.modalState.set(this.initialState);
  }

  /**
   * Execute confirm callback and close.
   */
  public confirm(): void {
    const state = this.modalState();
    if (state.onConfirm) {
      state.onConfirm();
    }
    this.close();
  }

  /**
   * Execute cancel callback and close.
   */
  public performCancel(): void {
    const state = this.modalState();
    if (state.onCancel) {
      state.onCancel();
    }
    this.close();
  }
}
