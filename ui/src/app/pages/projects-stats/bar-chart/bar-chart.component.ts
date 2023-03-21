import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';

@Component({
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.scss']
})
export class BarChartComponent implements OnInit, OnChanges {

  @Input() type: string;
  @Input() stats: any;

  public barChartOptions: any = {
    scaleShowVerticalLines: false,
    maintainAspectRatio: false,
    responsive: true,
    scales: {
      xAxes: [
        {
          barThickness: 13,
          gridLines: {
            display: false
          }
        }
      ],
      yAxes: [{
        ticks: {
          beginAtZero: true
        }
      }]
    }
  };

  public blankBarChartOptions: any = {
    scaleShowVerticalLines: false,
    maintainAspectRatio: false,
    responsive: true,
    scales: {
      xAxes: [
        {
          barThickness: 13,
          scaleFontSize: 40,
          gridLines: {
            display: false
          },
          ticks:{
            fontSize: 28
          }
        }
      ] 
    },
  };

  // public mbarChartLabels: string[] = ['2012', '2013', '2014', '2015', '2016', '2017', '2018'];
  public mbarChartLabels: string[] = [];
  public barChartType = 'bar';
  public barChartLegend = false;

  public barChartColors: Array<any> = [
    {
      backgroundColor: 'rgba(112, 115, 156)',
      borderColor: 'rgba(105,159,177,1)',
      pointBackgroundColor: 'rgba(105,159,177,1)',
      pointBorderColor: '#fafafa',
      pointHoverBackgroundColor: '#fafafa',
      pointHoverBorderColor: 'rgba(105,159,177)'
    },
    // {
    //   backgroundColor: 'rgba(77,20,96,0.3)',
    //   borderColor: 'rgba(77,20,96,1)',
    //   pointBackgroundColor: 'rgba(77,20,96,1)',
    //   pointBorderColor: '#fff',
    //   pointHoverBackgroundColor: '#fff',
    //   pointHoverBorderColor: 'rgba(77,20,96,1)'
    // }
  ];

  // public barChartData: any[] = [
  //   { data: [55, 60, 75, 82, 56, 62, 80], label: 'Company A' },
  //   { data: [58, 55, 60, 79, 66, 57, 90], label: 'Company B' },
  // ];

  public blankBarChartData: any[] = [
    { data: [] },
  ];

  public blankBarChartLabels: string[] = ['No Chart Record Available'];

  public barChartData: any[] = [];

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.stats) {
      return;
    }

    if (this.type === 'Source Data') {
      this.barChartData = [{
        data: Object.values(this.stats?.sourceDataset?.summary ?? [])
      }];
      this.mbarChartLabels = Object.keys(this.stats?.sourceDataset?.summary ?? []);
    } else if (this.type === 'Prepared Datasets') {
      const yValues = Object.values(this.stats?.preparedDataset?.summary);
      const xValues = Object.keys(this.stats?.preparedDataset?.summary);

      this.barChartData = [{
        data: [...yValues]
      }];
      this.mbarChartLabels = [...xValues];
    } else if (this.type === 'Data Volume') {
      
      this.barChartData = [{
        data: Object.values(this.stats?.dataVolume?.summary ?? [])
      }];
      this.mbarChartLabels = Object.keys(this.stats?.dataVolume?.summary ?? []);
    } else if (this.type === 'Engineering Flows') {
      const yValues = Object.values(this.stats?.dataPipes?.summary);
      const xValues = Object.keys(this.stats?.dataPipes?.summary);

      this.barChartData = [{
        data: [...yValues] ?? []
      }];
      this.mbarChartLabels = [...xValues];
    }
  }
}
