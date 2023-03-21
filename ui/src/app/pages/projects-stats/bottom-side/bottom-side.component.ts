import { Component, OnInit, Input} from '@angular/core';

@Component({
  selector: 'app-bottom-side',
  templateUrl: './bottom-side.component.html',
  styleUrls: ['./bottom-side.component.scss']
})
export class BottomSideComponent implements OnInit {

  @Input() efforts: any;

  constructor() { }

  ngOnInit(): void {
  }

}
