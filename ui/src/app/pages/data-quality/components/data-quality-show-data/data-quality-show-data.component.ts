import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-data-quality-show-data',
  templateUrl: './data-quality-show-data.component.html',
  styleUrls: ['./data-quality-show-data.component.scss']
})
export class DataQualityShowDataComponent implements OnInit {

  @Input() sourceTargetType: string;
  @Output() close = new EventEmitter<void>();
  public active = 1;
  public validBtn: boolean;

  public tableData1 = [
    { id: 1, name: 'John Doe', lastname: 'Doe', role: 'dev', age: 30, city: 'New York', country: 'USA', occupation: 'Engineer', status: 'Active' },
    { id: 2, name: 'Jane Smith', lastname: 'Doe', role: 'dev', age: 28, city: 'Los Angeles', country: 'USA', occupation: 'Designer', status: 'Inactive' },
    { id: 3, name: 'Michael Johnson', lastname: 'Doe', role: 'dev', age: 35, city: 'Chicago', country: 'USA', occupation: 'Manager', status: 'Active' },
    { id: 4, name: 'John Doe', lastname: 'Doe', role: 'dev', age: 30, city: 'New York', country: 'USA', occupation: 'Engineer', status: 'Active' },
    { id: 5, name: 'Jane Smith', lastname: 'Doe', role: 'dev', age: 28, city: 'Los Angeles', country: 'USA', occupation: 'Designer', status: 'Inactive' },
    { id: 6, name: 'Michael Johnson', lastname: 'Doe', role: 'dev', age: 35, city: 'Chicago', country: 'USA', occupation: 'Manager', status: 'Active' },
  ];

  public tableData2 = [
    { id: 4, name: 'Alice Johnson', age: 25, city: 'San Francisco', country: 'USA', occupation: 'Developer', status: 'Active' },
    { id: 5, name: 'Robert Williams', age: 32, city: 'Toronto', country: 'Canada', occupation: 'Accountant', status: 'Inactive' },
    { id: 6, name: 'Emily Davis', age: 28, city: 'London', country: 'UK', occupation: 'Writer', status: 'Active' },
  ];

  public tableDataColumn1 = Object.keys(this.tableData1[0]);
  public tableDataColumn2 = Object.keys(this.tableData2[0]);

  ngOnInit(): void {
    if(this.sourceTargetType == "sourceAndTarget"){
      this.validBtn = true;
    } else {
      this.validBtn = false;
    }
  }

  target(): void {
    this.validBtn = false;
  }

  source(): void {
    this.validBtn = true;
  }
}
