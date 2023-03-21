import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-confirmation-dialog',
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent implements OnInit {

  @Input() title: string;
  @Input() message: string;
  @Input() btnOkText: string;
  @Input() btnCancelText: string;

  @Input() showOkButton = true;
  @Input() showCancelButton = true;

  constructor(private modal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  public decline() {
    this.modal.close(false);
  }

  public accept() {
    this.modal.close(true);
  }

  public dismiss() {
    this.modal.dismiss();
  }
}
