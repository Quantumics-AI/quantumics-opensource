import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.scss']
})
export class ConfirmationComponent {
  constructor(
    public modal: NgbActiveModal,
    private snakbar: SnackbarService,
  ) { }

  ngOnInit(): void {
  }

  public deleteSource(): void {
    this.modal.close();
  }
}
