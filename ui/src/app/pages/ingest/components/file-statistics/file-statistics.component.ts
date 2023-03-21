import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-file-statistics',
  templateUrl: './file-statistics.component.html',
  styleUrls: ['./file-statistics.component.scss']
})
export class FileStatisticsComponent implements OnInit {

  headers = [];
  columns = [];
  data = [];

  constructor(public modal: NgbActiveModal) { }

  ngOnInit(): void {
    this.headers = ['Column Name', 'Null', 'Missing', 'Count', 'Unique', 'Top', 'Frequency', 'Mean', 'Std', 'Min'];
    this.data = [
      ['Product Code', 2, 6, 12, 14, 12, 15, 14, 18, 23],
      ['HSN Code', 2, 6, 12, 14, 12, 15, 14, 18, 23],
      ['Product Code', 2, 6, 12, 14, 12, 15, 14, 18, 23],
      ['HSN Code', 2, 6, 12, 14, 12, 15, 14, 18, 23],

    ];
  }
}
