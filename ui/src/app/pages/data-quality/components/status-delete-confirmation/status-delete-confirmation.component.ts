import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-status-delete-confirmation',
  templateUrl: './status-delete-confirmation.component.html',
  styleUrls: ['./status-delete-confirmation.component.scss']
})
export class StatusDeleteConfirmationComponent {

  constructor(
    public modal: NgbActiveModal,
    private snakbar: SnackbarService,
  ) { }

  ngOnInit(): void {
  }

  public deleteRule(): void {
    this.modal.close();
  }
}
