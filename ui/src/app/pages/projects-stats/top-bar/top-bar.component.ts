import { Component, EventEmitter, OnInit, Output, Input } from '@angular/core';

@Component({
  selector: 'app-top-bar',
  templateUrl: './top-bar.component.html',
  styleUrls: ['./top-bar.component.scss']
})
export class TopBarComponent implements OnInit {

  @Output() selectedType = new EventEmitter<string>();

  @Input() totalfile: any;
  
  sourceData: string = 'Source Data';
  preparedData: string = 'Prepared Datasets';
  dataPipes: string = 'Engineering Flows';
  dataVolume: string = 'Data Volume';
  noOfPii: string = 'No. Of PII'

  public selectedIndex: number;
  selectedIndexOne: number = 0;
  selectedIndexTwo: number = 2;
  selectedIndexThree: number = 3;
  selectedIndexFour: number = 4;
  selectedIndexFive: number = 5;
  

  public data = [{
    image: 'source-folder.svg',
    label: 'Source data',
    value: 120
  },
  {
    image: 'datasets.svg',
    label: 'Prepared Datasets',
    value: 450
  },
  {
    image: 'data-pipes.svg',
    label: 'Engineering Flows',
    value: 300
  },
  {
    image: 'data-volume.svg',
    label: 'Data Volume',
    value: 300
  },
  {
    image: 'pii.svg',
    label: 'No. Of PII',
    value: 100
  }];

  constructor() {}

  ngOnInit(): void {
    this.selectedIndex = 0;
    // const type = this.data[this.selectedIndex];
    this.selectedType.emit(this.sourceData);
  }

  public selectType(name: string): void {
    if(name == 'Source Data'){
      this.selectedIndex = 0;
    }
    if(name == 'Prepared Datasets'){
      this.selectedIndex = 2;
    }

    if(name == 'Engineering Flows'){
      this.selectedIndex = 3;
    }

    if(name == 'Data Volume'){
      this.selectedIndex = 4;
    }

    if(name == 'No. Of PII'){
      this.selectedIndex = 5;
    }
    // this.selectedIndex = index;
    // const type = this.data[this.selectedIndex];
    this.selectedType.emit(name);
  }
}
