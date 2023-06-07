import { Component, EventEmitter, OnInit, Output, Input, SimpleChanges, OnChanges } from '@angular/core';
import { KpiFilter } from '../Models/kpi-filter.model';

@Component({
  selector: 'app-top-bar',
  templateUrl: './top-bar.component.html',
  styleUrls: ['./top-bar.component.scss']
})
export class TopBarComponent implements OnInit, OnChanges {

  @Output() selectedType = new EventEmitter<string>();
  @Input() kpiData: Array<KpiFilter> = [];
  @Input() totalfile: any;
  @Input() applyKpiFilter: boolean;
  @Input() loadingData: boolean;

  // dashboard: string = 'Dashboard';
  // sourceData: string = 'Source Data';
  // preparedData: string = 'Prepared Datasets';
  // dataPipes: string = 'Engineering Flows';
  // dataVolume: string = 'Storage';
  // noOfPii: string = 'No. Of PII';
  // folder: string = 'Folder';
  // pipeline: string = 'Pipeline'

  public selectedIndex: number;
  // selectedIndexOne: number = 0;
  // selectedIndexTwo: number = 2;
  // selectedIndexThree: number = 3;
  // selectedIndexFour: number = 4;
  // selectedIndexFive: number = 5;
  // selectedIndexSix: number = 6;
  // selectedIndexSeven: number = 7;
  // selectedIndexEight: number = 8;

  public selectedKpi: any;
  // public loading: boolean;

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

  constructor() {
  }

  ngOnInit(): void {
    // this.selectedIndex = 0;
    this.selectedKpi = this.kpiData.filter((k) => { return k.selected == true });
    this.selectedIndex = this.selectedKpi[0].id;
    // const type = this.data[this.selectedIndex];
    // this.selectedType.emit(this.sourceData);

    this.selectedType.emit(this.selectedKpi[0].label);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes.applyKpiFilter?.firstChange) {
      this.selectedKpi = this.kpiData.filter((k) => { return k.selected == true });
      let selectedTopbarOption = this.selectedKpi.find(x => x.selectedTopbarOption === true);
      selectedTopbarOption = selectedTopbarOption ?? this.selectedKpi[0];
      this.selectedIndex = selectedTopbarOption.id;
      this.selectedType.emit(selectedTopbarOption?.label);
    }
  }

  public selectType(name: string, id: number): void {
    this.selectedIndex = id;
    this.selectedType.emit(name);
  }
}
