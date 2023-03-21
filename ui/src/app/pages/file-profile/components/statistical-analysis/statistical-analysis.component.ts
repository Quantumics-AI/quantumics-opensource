import { Component, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { StatisticsParams } from '../../models/file-stats';

@Component({
  selector: 'app-statistical-analysis',
  templateUrl: './statistical-analysis.component.html',
  styleUrls: ['./statistical-analysis.component.scss'],
  encapsulation: ViewEncapsulation.None,
  styles: [
		`
			.my-custom-class .tooltip-inner {
        width: 386px !important;
        max-width: 386px !important;
        text-align: start;
			}
		`,
	],
})
export class StatisticalAnalysisComponent implements OnInit {
  @Input() statisticsParams: StatisticsParams;
  @Input() dataType: string;
  @Input() analysis: any;

  constructor() {
  }

  ngOnInit(): void {
  }

}
