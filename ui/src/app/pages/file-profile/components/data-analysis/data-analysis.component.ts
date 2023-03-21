import { Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import { AttributeCount } from '../../models/file-stats';

@Component({
  selector: 'app-data-analysis',
  templateUrl: './data-analysis.component.html',
  styleUrls: ['./data-analysis.component.scss'],
  encapsulation: ViewEncapsulation.None,
  styles: [
		`
			.my-custom-class .tooltip-inner {
        width: 324px !important;
        max-width: 324px !important;
        text-align: start;
			}
		`,
	],
})
export class DataAnalysisComponent implements OnInit {
  @Input() attributeCount: AttributeCount;
  @Input() recordCount: number;
  @Input() analysis: any;

  constructor() {
  }

  ngOnInit(): void {
  }

}
