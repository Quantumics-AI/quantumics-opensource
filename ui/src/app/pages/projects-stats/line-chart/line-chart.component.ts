import { AfterViewInit, Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { Color, Label } from 'ng2-charts';

@Component({
  selector: 'app-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss']
})
export class LineChartComponent implements OnChanges, AfterViewInit {

  @Input() type: string;
  @Input() stats: any;
  public showLine: boolean;

  @ViewChild('myCanvas') canvas: ElementRef;

  public lineChartData: any[] = [];
  public lineChartLabels: Label[] = [];
  public lineChartLegend = false;
  public lineChartColors: Color[] = [
    {
      borderWidth: 1,
      borderColor: '#59D386',
      pointRadius: 3,
      pointBorderWidth: 0,
    }
  ];

  public lineChartOptions:any = {            
    // title: {
    //   display: true,
    //   // text: 'Line Chart Example'
    // },
    // legend: {
    //   position: 'bottom'
    // },
    maintainAspectRatio: false,
    responsive: true,
      scales: {
        xAxes: [
          {
            // barThickness: 13,
            gridLines: {
              // display: false
              color:'#F2F4F7'
            }
            // padding: 40
          }
        ],
        yAxes: [ {
          ticks: {
            fontSize: 12,
            lineHeight: 2,
            beginAtZero: true
          },
          gridLines: {
            color:'#F2F4F7'
          }
        }]
    }
  };

  public yesterdayCount: number;
  public avgRange: string;

  ngAfterViewInit() {
    const gradient = this.canvas.nativeElement
      .getContext('2d')
      .createLinearGradient(0, 0, 0, 500);
    gradient.addColorStop(0, '#59D386');
    gradient.addColorStop(1, 'rgba(217, 217, 217, 0)');
    this.lineChartColors = [
      {
        ...this.lineChartColors[0],
        backgroundColor: gradient
      }
    ];
  }

  // events
  public chartClicked(e:any):void {
    // console.log(e);
  }

  public chartHovered(e:any):void {
    // console.log(e);
  }

  ngOnInit(): void {
    
  }

  ngOnChanges(changes: SimpleChanges): void {

    if (!this.stats) {
      return;
    }

    if (this.type === 'Dataset') {
      this.showLine = true;
      this.avgRange = this.stats?.sourceDataset?.averageRange;
      this.yesterdayCount = this.stats?.sourceDataset?.yesterdayCount;
      this.lineChartData = [{
        data: Object.values(this.stats?.sourceDataset?.summary ?? [])
      }];
      this.lineChartLabels = Object.keys(this.stats?.sourceDataset?.summary ?? []);
    } else if (this.type === 'Prepared Datasets') {
      this.showLine = false;
      this.avgRange = this.stats?.preparedDataset?.averageRange;
      this.yesterdayCount = this.stats?.preparedDataset?.yesterdayCount;
      
      const yValues = Object.values(this.stats?.preparedDataset?.summary);
      const xValues = Object.keys(this.stats?.preparedDataset?.summary);

      this.lineChartData = [{
        data: [...yValues]
      }];
      this.lineChartLabels = [...xValues];
    } else if (this.type === 'Storage') {
      this.showLine = true;
      // this.avgRange = this.stats?.dataVolume?.averageRange;
      const avgRangeArray = this.stats?.dataVolume?.averageRange.split(" - "); // Split the range into an array ["0.1124", "1.1124"]
      const lowerBound = Number(avgRangeArray[0]).toFixed(2); // Round the lower bound to 2 decimal places and convert it to a number
      const upperBound = Number(avgRangeArray[1]).toFixed(2);
      this.avgRange = lowerBound + " - " + upperBound;
      this.yesterdayCount = this.stats?.dataVolume?.yesterdayCount;

      this.lineChartData = [{
        data: Object.values(this.stats?.dataVolume?.summary ?? [])
      }];
      this.lineChartLabels = Object.keys(this.stats?.dataVolume?.summary ?? []);
    } else if (this.type === 'Data flow') {
      this.showLine = false;
      this.avgRange = this.stats?.dataPipes?.averageRange;
      this.yesterdayCount = this.stats?.dataPipes?.yesterdayCount;
      const yValues = Object.values(this.stats?.dataPipes?.summary);
      const xValues = Object.keys(this.stats?.dataPipes?.summary);

      this.lineChartData = [{
        data: [...yValues] ?? []
      }];
      this.lineChartLabels = [...xValues];
    } else if (this.type === 'Dashboard') {
      this.showLine = false;
      this.avgRange = this.stats?.dashboard?.averageRange;
      this.yesterdayCount = this.stats?.dashboard?.yesterdayCount;
      this.lineChartData = [{
        data: Object.values(this.stats?.dashboard?.summary ?? [])
      }];
      this.lineChartLabels = Object.keys(this.stats?.dashboard?.summary ?? []);
    } else if (this.type === 'Folder') {
      this.showLine = false;
      this.avgRange = this.stats?.folders?.averageRange;
      this.yesterdayCount = this.stats?.folders?.yesterdayCount;
      this.lineChartData = [{
        data: Object.values(this.stats?.folders?.summary ?? [])
      }];
      this.lineChartLabels = Object.keys(this.stats?.folders?.summary ?? []);
    } else if (this.type === 'Pipeline') {
      this.showLine = false;
      this.avgRange = this.stats?.pipeline?.averageRange;
      this.yesterdayCount = this.stats?.pipeline?.yesterdayCount;
      this.lineChartData = [{
        data: Object.values(this.stats?.pipeline?.summary ?? [])
      }];
      this.lineChartLabels = Object.keys(this.stats?.pipeline?.summary ?? []);
    }
  }
}
