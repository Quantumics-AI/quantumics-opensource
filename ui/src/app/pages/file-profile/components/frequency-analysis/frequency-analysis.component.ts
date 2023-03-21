import { Component, Input, OnChanges, SimpleChanges, ViewEncapsulation } from '@angular/core';
import { Frequency } from '../../models/file-stats';
import { orderBy } from 'lodash';

@Component({
  selector: 'app-frequency-analysis',
  templateUrl: './frequency-analysis.component.html',
  styleUrls: ['./frequency-analysis.component.scss'],
  // encapsulation: ViewEncapsulation.None,
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
export class FrequencyAnalysisComponent implements OnChanges {
  @Input() frequency: Frequency[];
  @Input() analysis: any;
  @Input() totalRecords: number;
  @Input() loading: boolean;

  public isDescending: boolean;

  ngOnInit(): void {
    console.log("====", this.frequency)
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes?.loading?.currentValue) {
      this.frequency = orderBy(this.frequency, [item => item?.value, item => item?.text] , ['desc', 'asc']);
    }
  }

  public sort(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.frequency = this.frequency.sort((a, b) => {
        return new Date(a.value) as any - <any>new Date(b.value);
      });
    } else {
      this.frequency = this.frequency.sort((a, b) => {
        return (new Date(b.value) as any) - <any>new Date(a.value);
      });
    }

  }
}
