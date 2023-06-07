import { Component, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Analysis, Column, ColumnsStats } from '../../models/file-stats';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-view-file-profile',
  templateUrl: './view-file-profile.component.html',
  styleUrls: ['./view-file-profile.component.scss'],
  encapsulation: ViewEncapsulation.None,
  styles: [
    `
			.my-custom-class .tooltip-inner {
        width: 269px !important;
        max-width: 269px !important;
        text-align: start;
			}
      .my-custom-list-class .tooltip-inner {
        width: 349px !important;
        max-width: 349px !important;
        text-align: start;
			}
      .my-custom-datasetclass .tooltip-inner {
        width: 217px !important;
        max-width: 217px !important;
        text-align: start;
			}
		`,
  ],
})
export class ViewFileProfileComponent implements OnInit {

  @Input() projectId: number;
  @Input() folderId: number;
  @Input() fileId: number;

  public loading: boolean;
  public loadingFrequency: boolean;
  public columnDataType: string;
  public columnsStats: ColumnsStats;
  public analysis: Analysis;
  public tableListData: Array<any> = [];
  public dataTypeData: Array<any> = [];
  public uniqueTypes: Array<string> = [];
  private userId: number;
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();

  public columnAnalysisContent: string = "Select column from the dataset to get the custom column analysis or click on list button on right for whole dataset analysis.";
  public datasetContent: string = "Anaysis of all column records with numerical statistics. Sort based on data type, column name & missing values.";
  public columnview: boolean = true;
  public listview: boolean = false;
  public listData: any;
  public isDescending: boolean;
  public sourceType: string;

  constructor(
    private activatedRoute: ActivatedRoute,
    private profileService: ProfileService,
    private quantumFacade: Quantumfacade) {

    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
    this.analysis = new Analysis();

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((certificate: Certificate) => {
        if (certificate) {
          this.userId = +certificate.user_id;
        }
      });

    if (this.folderId && this.fileId) {
      this.getfileStats();
      this.getFrequencyAnalysis('');
    }

    this.activatedRoute.queryParams.subscribe(params => {
      this.folderId = +params.folderId;
      this.fileId = +params.fileId;
      this.sourceType = params.sourcetype;
      if (this.folderId && this.fileId) {
        this.getfileStats();
        this.getFrequencyAnalysis('');
      }
    });
  }

  ngOnInit(): void {

  }

  selectColumn(c: Column): void {
    this.analysis.columns.map(t => {
      t.selected = (t.column === c.column);
    });

    this.columnDataType = c.dataType;
    this.getFrequencyAnalysis(c.column);
    this.columnsStats = this.analysis.columnsStats.find(x => x.column === c.column);
  }

  private getFrequencyAnalysis(column: string) {
    this.loadingFrequency = true;
    this.analysis.frequency = [];

    if (!column) {
      column = 'EMPTY';
    }

    this.profileService.getFrequencyAnalysis(this.projectId, this.userId, this.folderId, this.fileId, column).subscribe((response: any) => {
      this.loadingFrequency = false;

      if (response?.analytics) {
        // update this code once API give proper response.
        // temporary fix as per dicussion with Prakash.
        let data = response?.analytics?.replace(/NaN/g, '0.0');
        data = JSON.parse(response?.analytics);

        const key = Object.keys(data?.column_value_frequency)[0];
        this.analysis.selectedDataType = this.tableListData.find(x => x.column === key)?.type;
        this.analysis.selectedColumn = key;
        const d = data?.column_value_frequency[key]?.valueFrequency;

        this.analysis.frequency = Object.keys(d).map(k => {
          return {
            text: k,
            value: d[k]
          };
        });
        console.log("LE", this.analysis.frequency.length);

      }
    }, () => {
      this.loadingFrequency = false;
    });
  }

  public getfileStats(): void {
    this.loading = true;
    this.analysis = new Analysis();
    this.uniqueTypes = [];

    this.profileService.getFileStats(
      this.projectId, this.userId, this.folderId, this.fileId).subscribe((response: any) => {
        this.loading = false;
        if (response?.analytics) {
          try {
            // temporary fix as per dicussion with Prakash
            let data = response?.analytics?.replace(/NaN/g, '0.0');
            data = JSON.parse(data);
            this.dataTypeData = [];
            this.tableListData = [];
            // this.listData = data?.file1_stats[1].columnsStats;
            this.analysis.columns.map(t => t.selected = false);
            this.formatResponse(data);
            const firstColumn = this.analysis.columns[0];
            this.columnDataType = firstColumn.dataType;
            this.columnsStats = this.analysis.columnsStats.find(x => x.column === firstColumn.column);
          } catch (e) {
            this.analysis = new Analysis();
          }
        }
      }, () => {
        this.loading = false;
      });
  }

  // TODO - this method will remove once API reposne correct.
  private formatResponse(data: any): void {

    const fileStats = data?.file1_stats[0]?.fileStats;
    console.log("filestats", data?.file1_stats[0]?.fileStats);

    const columnsStats = data?.file1_stats[1]?.columnsStats;
    const columns = fileStats?.columns;

    Object.keys(columns).map((k, index) => {

      const columnMetaData = columnsStats[index][k];
      const t = {
        column: k,
        type: columns[k],
        distinct: columnMetaData['attributeCount']['distinct'],
        duplicate: columnMetaData['attributeCount']['duplicate'],
        notNull: columnMetaData['attributeCount']['notNull'],
        null: columnMetaData['attributeCount']['null'],
        max: columnMetaData['statisticsParams']['max'],
        mean: columnMetaData['statisticsParams']['mean'],
        median: columnMetaData['statisticsParams']['median'],
        min: columnMetaData['statisticsParams']['min'],
        variance: columnMetaData['statisticsParams']['variance'],
        std: columnMetaData['statisticsParams']['std'],
        sum: columnMetaData['statisticsParams']['sum']
      };

      this.tableListData.push(t);
    });

    this.uniqueTypes = [...new Set(this.tableListData.map(obj => obj.type))];

    this.uniqueTypes.map(t => {
      const matchingColumns = this.tableListData.filter(x => x.type === t);
      const data = {
        type: t,
        percentage: (matchingColumns?.length / this.tableListData.length) * 100,
        columnCount: matchingColumns?.length
      };

      this.dataTypeData.push(data);
    });

    this.analysis.recordCount = fileStats?.recordCount;
    this.analysis.columnCount = fileStats?.columnCount;
    this.analysis.fileSize = fileStats?.fileSize;

    this.analysis.columns = Object.keys(columns).map((k, index) => {
      return {
        column: k,
        dataType: columns[k],
        selected: index == 0
      };
    });

    this.listData = this.analysis.columns;


    this.analysis.columnsStats = Object.keys(columnsStats).map(t => {
      const column = Object.keys(columnsStats[t])[0];
      return {
        column,
        attributeCounts: columnsStats[t][column].attributeCount,
        statisticsParams: columnsStats[t][column].statisticsParams
      } as ColumnsStats;
    });
  }

  public columnView(): void {
    this.columnview = true;
    this.listview = false;
  }

  public listView(): void {
    this.listview = true;
    this.columnview = false;
  }

  public sortDataTypes(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.tableListData = this.tableListData.sort((a, b) => {
        var folder_name_order = a.type.localeCompare(b.type);
        return folder_name_order;
      });
    } else {
      this.tableListData = this.tableListData.sort((a, b) => {
        var folder_name_order = b.type.localeCompare(a.type);
        return folder_name_order;
      });
    }
  }

  public sortColumn(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.tableListData = this.tableListData.sort((a, b) => {
        var folder_name_order = a.column.localeCompare(b.column);
        return folder_name_order;
      });
    } else {
      this.tableListData = this.tableListData.sort((a, b) => {
        var folder_name_order = b.column.localeCompare(a.column);
        return folder_name_order;
      });
    }
  }


  public sortMissing(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.tableListData = this.tableListData.sort((a, b) => {
        return new Date(a.null) as any - <any>new Date(b.null);
      });
    } else {
      this.tableListData = this.tableListData.sort((a, b) => {
        return (new Date(b.null) as any) - <any>new Date(a.null);
      });
    }
  }

  public redirectToColumnView(item: any): void {
    this.listview = false;
    this.columnview = true;

    const column = {
      column: item.column,
      dataType: item.type
    } as Column;

    this.selectColumn(column);
  }
}
