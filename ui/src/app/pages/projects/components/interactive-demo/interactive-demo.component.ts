import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-interactive-demo',
  templateUrl: './interactive-demo.component.html',
  styleUrls: ['./interactive-demo.component.scss']
})
export class InteractiveDemoComponent implements OnInit {

  constructor(public modal: NgbActiveModal) { }

  ngOnInit(): void {
  }

}
