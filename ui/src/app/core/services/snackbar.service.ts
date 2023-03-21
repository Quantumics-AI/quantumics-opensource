import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class SnackbarService {

  constructor(private snackBar: MatSnackBar) { }

  open(message: string, action = '', duration = 3000): void {
    this.snackBar.open(message, action, {
      duration,
      verticalPosition: 'bottom',
      horizontalPosition: 'right'
    });
  }
}
