import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-preview',
  templateUrl: './preview.component.html',
  styleUrls: ['./preview.component.scss']
})
export class PreviewComponent {
  @Input() tital = '';
  @Input() data: any = [];
  @Input() columns: any = [];
  public column_length: number;
  public row_length:number;

  constructor(public modal: NgbActiveModal) {
  }
  ngOnInit(): void {
    console.log(this.columns.length);
    console.log(this.data.length);

    this.column_length = this.columns.length;
    this.row_length = this.data.length
    
  }
  
}
