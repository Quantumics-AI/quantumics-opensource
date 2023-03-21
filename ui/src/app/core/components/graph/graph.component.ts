import { Component, Input, OnInit } from '@angular/core';
import { ChartOptions, ChartType } from 'chart.js';

@Component({
  selector: 'app-graph',
  templateUrl: './graph.component.html',
  styleUrls: ['./graph.component.scss']
})
export class GraphComponent implements OnInit {

  @Input() data: any;
  @Input() column: any;
  newColumns = [];
  loading: boolean;

  public barChartLegend = false;
  public barChartPlugins = [];
  public barChartType: ChartType = 'bar';
  public barChartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    legend: {
      display: false
    },
    tooltips: {
      enabled: true
    },
    scales: {
      xAxes: [
        {
          display: false
        }
      ],
      yAxes: [
        {
          display: false
        }
      ]
    }
  };

  ngOnInit(): void {
    this.loading = true;
    const datatypes = [];
    this.newColumns = [];

    const groupBy = (array, key) => {
      return array.reduce((result, currentValue) => {
        (result[currentValue[key]] = result[currentValue[key]] || []).push(
          currentValue
        );
        return result;
      }, {});
    };

    const groupArr = groupBy(this.data, this.column.column_name);
    const barChartLabel = [];
    const barChartData = [
      {
        data: [],
        label: '',
        maxBarThickness: null,
        backgroundColor: 'green'
      }
    ];

    for (const key in groupArr) {
      barChartLabel.push(groupArr[key][0][this.column.column_name]);
      barChartData[0].data.push(parseInt(groupArr[key].length));
      barChartData[0].label = this.column.column_name;
      barChartData[0].maxBarThickness = 3;
    }

    this.newColumns.push({
      column_name: this.column.column_name,
      display_name: this.column.column_name,
      data_type: this.column.data_type,
      barChartLabel,
      barChartData
    });

    datatypes.push(this.column.data_type);
    this.loading = false;
  }
}
