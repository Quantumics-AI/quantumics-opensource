import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-show-file',
  templateUrl: './show-file.component.html',
  styleUrls: ['./show-file.component.scss']
})
export class ShowFileComponent implements OnInit {
  // @Input() sourceTargetType: string;
  @Output() close = new EventEmitter<void>();
  @Input() file: string;
  @Input() filePath: string;
  @Input() headers: any;
  @Input() fileContent: any;
  @Input() columnType: any;
  @Input() rowCount: number;
  @Input() totalRowCount: any;
  @Input() patternPath: string;

  public projectId: number;
  userId: number;
  public active = 1;
  public validBtn: boolean;
  public getSource: string;

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

  constructor(
    private readonly router: Router,
    private readonly activatedRoute: ActivatedRoute,
  ){
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
    this.getSource = sessionStorage.getItem('source');
  }

  ngOnInit(): void {
    // if(this.sourceTargetType == "sourceAndTarget"){
    //   this.validBtn = true;
    // } else {
    //   this.validBtn = false;
    // }
    console.log("Selected File:", this.file);
    console.log("Selected Path:", this.filePath);
    
  }

  target(): void {
    this.validBtn = false;
  }

  source(): void {
    this.validBtn = true;
  }

  public submitFile(): void {
    if(this.getSource == 'source-1'){
      sessionStorage.setItem('headersData', JSON.stringify(this.headers));
      sessionStorage.setItem('columnData', JSON.stringify(this.columnType));
    }
    sessionStorage.setItem('selectedFile', this.file);
    sessionStorage.setItem('SelectedPath', this.filePath);
    sessionStorage.setItem('selectedPattern', this.patternPath);
    sessionStorage.setItem('check', 'true');
    this.router.navigate([`projects/${this.projectId}/data-quality/create`]);
  }

}
