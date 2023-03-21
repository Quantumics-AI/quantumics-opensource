import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-email-modal',
  templateUrl: './email-modal.component.html',
  styleUrls: ['./email-modal.component.scss']
})
export class EmailModalComponent implements OnInit {

  @Input() title: string;
  @Input() message: string;
  @Input() showUpdatePassDiv: boolean;
  @Input() showFailDiv: boolean;

  constructor(public modal: NgbActiveModal) { }

  ngOnInit(): void {
  }

}
