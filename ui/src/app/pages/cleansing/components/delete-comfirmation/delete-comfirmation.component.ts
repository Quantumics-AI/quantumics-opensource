import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-delete-comfirmation',
  templateUrl: './delete-comfirmation.component.html',
  styleUrls: ['./delete-comfirmation.component.scss']
})
export class DeleteComfirmationComponent implements OnInit {

  constructor(public modal: NgbActiveModal) { }

  ngOnInit(): void {
  }

}
