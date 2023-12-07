import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { ProfileService } from 'src/app/pages/file-profile/services/profile.service';
import { StatisticsResponse } from '../../models/statistics-response';
import { AttributeResponse } from '../../models/attribute-response';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-column-analysis',
  templateUrl: './column-analysis.component.html',
  styleUrls: ['./column-analysis.component.scss']
})
export class ColumnAnalysisComponent implements OnInit, OnChanges, OnDestroy {
  @Input() public userId: number;
  @Input() public projectId: number;
  @Input() public folderId: number;
  @Input() public fileId: number;
  @Input() public column: string;
  @Input() public recordCount: number;

  public statisticsResponse: StatisticsResponse;
  public attributeCount: AttributeResponse;
  public loading: boolean;

  private unsubscribe: Subject<void> = new Subject();

  constructor(private profileService: ProfileService) { }

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {
    this.loading = true;
    this.attributeCount = null;
    this.statisticsResponse = null;

    this.profileService.getFileStats(this.projectId, this.userId, this.folderId, this.fileId)
    .pipe(takeUntil(this.unsubscribe))
    .subscribe((response: any) => {
      try {
        this.loading = false;

        if (response.status !== 200 || !response.analytics) {
          return;
        }

        let data = response?.analytics?.replace(/NaN/g, '0.0');
        data = JSON.parse(data);
        const file1_stats = data.file1_stats;
        const columnsStats = file1_stats[1]?.columnsStats;

        const columnObject = columnsStats.find(x => x[this.column]);
        const selectedColumnStats = columnObject[this.column];

        this.attributeCount = selectedColumnStats?.attributeCount;
        this.statisticsResponse = selectedColumnStats?.statisticsParams;
      } catch (error) {
        this.loading = false;;
        this.attributeCount = null
        this.statisticsResponse = null;
      }
    }, (error) => {
      this.loading = false;
      this.attributeCount = null;
      this.statisticsResponse = null;
    });
  }

  public formatNumber(number: number): string {
    return number === 0 ? number.toFixed(0) : number.toFixed(2);
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
