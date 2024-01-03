import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { AutomationService } from '../../services/automation.service';
import { PAGE_SIZE } from 'src/app/core/const/pageSize';

@Component({
  selector: 'app-dataset-list',
  templateUrl: './dataset-list.component.html',
  styleUrls: ['./dataset-list.component.scss']
})
export class DatasetListComponent implements OnInit {

  private userId: number;
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject<void>();

  public projectId: number;
  public dataFlowName: string;
  public dataFlowId: number;
  public loading: boolean;
  public searchTerm: any = { folderDisplayName: '' };
  public rawTab: boolean = true;
  public preTab: boolean;
  public engTab: boolean;
  public cleansedFiles: Array<any>;
  public engFiles: Array<any>;
  public rawFiles: Array<any>;
  public selectedFiles: any[];
  public rawFilesCount: number;
  public cleansedFilesCount: number;
  public engFilesCount: number;

  public isDescendingByFolderName: boolean;
  public isDescendingByDatasetName: boolean;

  public curentPage: number;
  public startIndex: number = 0;
  public pageSize: number = PAGE_SIZE;
  public endIndex: number = this.pageSize;
  public page = 1;

  public isEngJobResultAvailable: boolean = true;

  constructor(
    private router: Router,
    private snakbar: SnackbarService,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private automationService: AutomationService,
  ) {
    this.selectedFiles = [];
    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
      });
  }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      this.dataFlowName = params.flowname;
      this.dataFlowId = params.flowid;
    });

    this.getFilesData();
  }

  public getFilesData(): void {
    this.loading = true;
    this.automationService.getFilesToRun(this.projectId, this.userId, this.dataFlowId).subscribe((res) => {
      this.loading = false;

      if (res.code === 200) {
        this.isEngJobResultAvailable = true;
        this.cleansedFiles = res.result.processed.map(f => ({ ...f, category: 'processed' }));
        this.engFiles = res.result.eng.map(f => ({ ...f, category: 'eng' }));
        this.rawFiles = res.result.raw.map(f => ({ ...f, category: 'raw' }));

        this.engFiles.map(t => {
          t.folderDisplayName = t.engFlowName;
        });

        this.rawFilesCount = this.rawFiles.length;
        this.engFilesCount = this.engFiles.length;
        this.cleansedFilesCount = this.cleansedFiles.length;
      } else {
        this.isEngJobResultAvailable = false;
      }
    }, () => {
      this.loading = false;
      this.snakbar.open('Something went wrong, please try later');
      this.isEngJobResultAvailable = false;
    });
  }

  selectFile(evt, file: any): void {
    const isSelected: boolean = evt.target.checked;
    file.isChecked = evt.target.checked;

    if (isSelected) {
      const tempFile = {
        projectId: this.projectId,
        engFlowId: this.dataFlowId,
        name: file.fileName,
        type: 'file',
        fileType: file.category,
        fileId: file.fileId,
        folderId: file.folderId,
        eventId: file.eventId,
      };
      const isExists = this.selectedFiles.findIndex(x => x.eventId === file.eventId);

      if (isExists === -1) {
        this.selectedFiles.push(tempFile);
      }

    } else {
      this.selectedFiles = this.selectedFiles.filter(f => f.eventId !== file.eventId);
    }
  }

  public back(): void {
    this.router.navigate([`projects/${this.projectId}/automation/engineering`]);
  }

  public activeTabs(d: string): void {
    this.rawTab = false;
    this.preTab = false;
    this.engTab = false;

    this.searchTerm.folderDisplayName = '';
    this.isDescendingByFolderName = false;
    this.isDescendingByDatasetName = false;

    switch (d) {
      case 'raw':
        this.rawTab = true;
        break;
      case 'pre':
        this.preTab = true;
        break;
      case 'eng':
        this.engTab = true;
        break;
    }
  }

  public get rawFilesSelected(): number {
    return this.selectedFiles.filter(f => f.fileType === 'raw')?.length;
  }

  public get cleansedFilesSelected(): number {
    return this.selectedFiles.filter(f => f.fileType === 'processed')?.length;
  }

  public get engFilesSelected(): number {
    return this.selectedFiles.filter(f => f.fileType === 'eng')?.length;
  }

  public runJob(): void {
    const data = {
      projectId: this.projectId,
      userId: this.userId,
      engineeringId: this.dataFlowId,
      selectedFiles: this.selectedFiles,
      selectedJoins: [],
      selectedAggregates: [],
      selectedUdfs: []
    };

    this.loading = true;

    this.automationService.runAutomation(data).subscribe((res) => {
      // this.modal.close();
      this.loading = false;
      this.snakbar.open('Process started');
      localStorage.setItem('flowId', `${this.dataFlowId}`);
      this.router.navigate([`projects/${this.projectId}/automation/engineering`]);
    }, () => {
      this.loading = false;
    });
  }

  public sortByProperty<T>(array: T[], property: keyof T, isDescending: boolean): void {
    array.sort((a, b) => {
      const propA = a[property] as unknown as string;
      const propB = b[property] as unknown as string;

      if (typeof propA === 'string' && typeof propB === 'string') {
        return isDescending ? propB.localeCompare(propA) : propA.localeCompare(propB);
      }

      return 0;  // Return 0 if the properties are not strings
    });
  }

  public onPageChange(currentPage: number): void {
    this.curentPage = currentPage;
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  public clearSearhInput(): void {
    this.searchTerm.folderDisplayName = '';
  }
}
