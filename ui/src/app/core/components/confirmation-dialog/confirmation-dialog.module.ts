import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';

@NgModule({
    declarations: [ConfirmationDialogComponent],
    imports: [
        CommonModule
    ],
    exports: [ConfirmationDialogComponent]
})
export class ConfirmationDialogModule { }
