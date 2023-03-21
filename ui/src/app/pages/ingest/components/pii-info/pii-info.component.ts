import { Component, EventEmitter, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-pii-info',
  templateUrl: './pii-info.component.html',
  styleUrls: ['./pii-info.component.scss']
})
export class PiiInfoComponent implements OnInit {

  @Output() close = new EventEmitter<boolean>();
  constructor() { }

  ngOnInit(): void {
  }

}
