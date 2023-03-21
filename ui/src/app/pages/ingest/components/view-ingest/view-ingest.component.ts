import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { takeUntil } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';
import { PiiComponent } from '../pii/pii.component';
import { OutliersComponent } from '../outliers/outliers.component';
import { SourceDataService } from '../../services/source-data.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ChartOptions, ChartType } from 'chart.js';
import { AnalyticsService } from '../../services/analytics.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { FileStatisticsComponent } from '../file-statistics/file-statistics.component';
import { DeltaComponent } from '../delta/delta.component';
import { decrypt } from 'src/app/core/utils/decryptor';

@Component({
  selector: 'app-view-ingest',
  templateUrl: './view-ingest.component.html',
  styleUrls: ['./view-ingest.component.scss']
})
export class ViewIngestComponent implements OnInit, OnDestroy {

  projectId: any;
  folderId: string;
  file: any;
  fileId: number;
  loading = false;
  columns: any;
  data: any;
  folders: any;
  files = [];
  newColumns = [];
  graphData = [];
  pii: boolean;
  outliers: boolean;
  deltaFiles: boolean;

  rowLength: any;
  columnLength: any;
  projectSource$: Observable<any>;
  loaded$: Observable<boolean>;
  sourceData$: Observable<any>;
  jobStatus = false;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  folderName: string;
  undoArray = [];
  allJobs = [];
  isGraphVisible: boolean;
  isLoadingGraphData: boolean;

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

  private unsubscribe: Subject<void> = new Subject();
  editorComponent: any;
  showPreview: boolean;
  projectName: string;
  private piiColumns: Array<string>;
  constructor(
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private router: Router,
    private sourceDataService: SourceDataService,
    private modalService: NgbModal,
    private analyticsService: AnalyticsService,
    private snakbar: SnackbarService,
  ) {
    this.projectSource$ = this.quantumFacade.projectSource$;
    this.sourceData$ = this.quantumFacade.sourceData$;
    this.loaded$ = this.quantumFacade.loaded$;
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
      });

    this.activatedRoute.queryParams.subscribe(params => {
      this.projectId = params.projectId;
      this.folderName = params.folderName;
      this.folderId = params.folderId;
      this.file = params.file;
      this.fileId = params.fId;
      this.loadData();
    });
  }

  ngOnInit(): void {
    this.loading = true;
    this.projectName = localStorage.getItem('projectname');
  }

  sourceData(folderId, folderName) {
    this.router.navigate([`../source-data`], {
      queryParams: { folderId, name: folderName },
      relativeTo: this.activatedRoute
    });
  }

  viewPII(event: Event): void {
    const modalRef = this.modalService.open(PiiComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.folderId = this.folderId;
    modalRef.componentInstance.fileId = this.fileId;
    modalRef.result.catch((result) => {
      this.pii = false;
    });
    modalRef.result.then((result) => {
      this.pii = false;
    }, (reason) => { });
  }

  viewOutliers(event: Event): void {
    const modalRef = this.modalService.open(OutliersComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.folderId = this.folderId;
    modalRef.componentInstance.fileId = this.fileId;
    modalRef.result.catch(() => {
      this.outliers = false;
    });
    modalRef.result.then(() => {
      this.outliers = false;
    }, (reason) => { });
  }


  graphToggle(): void {
    this.modalService.open(FileStatisticsComponent, { size: 'lg', windowClass: 'modal-size' });
  }

  delta() {
    const modalRef = this.modalService.open(DeltaComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.folderId = this.folderId;
    modalRef.componentInstance.selectedFileId = this.fileId;
    modalRef.componentInstance.folderName = this.folderName;

    modalRef.result.catch(() => {
      this.deltaFiles = false;
    });
    modalRef.result.then(() => {
      this.deltaFiles = false;
    }, (reason) => { });
  }

  loadAnalyticsData(): void {
    this.isLoadingGraphData = true;
    this.analyticsService.getAnlyticsData(this.projectId,
      parseInt(this.folderId), this.fileId).subscribe((response) => {
        this.groupGraphData(response);
        this.isLoadingGraphData = false;
      }, () => {
        this.isLoadingGraphData = false;
        this.graphData = [];
      });
  }

  loadData(): void {
    this.isGraphVisible = false;

    const params = {
      userId: this.certificateData.user_id,
      projectId: this.projectId,
      folderId: this.folderId,
      folderName: this.folderName,
      fileId: this.fileId,
      file: this.file
    };

    const datatypes = [];
    this.newColumns = [];

    this.loading = true;
    this.sourceDataService.getFileContent(params).subscribe((response) => {
      this.data = JSON.parse(decrypt(response.data));
      this.columns = JSON.parse(decrypt(response.metadata));
      let temp;

      try {
        temp = JSON.parse(response.file_additional_info)?.encryptPiiColumns?.split(',');
      } catch (error) {
        
      }
      this.piiColumns = temp ? temp : [];

      this.newColumns = this.columns.map(c => {
        datatypes.push(c.data_type);

        return {
          column_name: c.column_name,
          display_name: c.column_name,
          data_type: c.data_type,
          barChartLabel: [],
          barChartData: []
        };
      });
      const uniques = [...new Set(datatypes)];
      this.columnLength = this.columns.length;
      this.rowLength = this.data.length;
      this.loading = false;
    }, () => {
      this.loading = false;
    });
  }

  groupGraphData(data): void {
    this.graphData = [];

    if (data.status != 200) {
      return;
    }

    for (const column of this.columns) {
      const barChartLabel = [];
      const barChartData = [
        {
          data: [],
          label: '',
          maxBarThickness: null,
          backgroundColor: 'green'
        }
      ];
      for (const element of data.analytics) {
        if (
          element.column_name.toLowerCase() ===
          column.column_name.toLowerCase()
        ) {
          barChartLabel.push(element.column_value);
          barChartData[0].data.push(parseInt(element.count));
          barChartData[0].label = element.column_name;
          barChartData[0].maxBarThickness = 3;
        }
      }
      this.graphData.push({
        column_name: column.column_name,
        display_name: column.column_name,
        data_type: column.data_type,
        barChartLabel,
        barChartData
      });
    }
  }


  public isPIIColumn(columnName: string): boolean {
    return this.piiColumns.includes(columnName);
  }

  back(): void {
    this.router.navigate([`projects/${this.projectId}/ingest`]);
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
