import { Component, OnInit, Input, SimpleChanges, OnChanges } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Router, ActivatedRoute } from '@angular/router';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { SourceDataService } from 'src/app/pages/ingest/services/source-data.service';

@Component({
  selector: 'app-list-raw-data',
  templateUrl: './list-raw-data.component.html',
  styleUrls: ['./list-raw-data.component.scss']
})
export class ListRawDataComponent implements OnInit, OnChanges {
  @Input() projectId: string;
  @Input() selectionChange: boolean;

  folders = [];
  files = [];
  folderId: any;
  folderName: any;
  projectName: string;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  selectedFileId: string;
  selectedFile: string;
  loading: boolean;
  isVisible: boolean;
  userId: number;

  private unsubscribe: Subject<void> = new Subject<void>();
  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private sourceDataService: SourceDataService
  ) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
        this.userId = +this.certificateData.user_id;
      });
  }

  ngOnInit(): void {
    this.loading = true;
    this.projectName = localStorage.getItem('projectname');
    this.activatedRoute.queryParams.subscribe(params => {
      this.folderName = params.name;
      this.folderId = params.folderId;
    });


    this.sourceDataService.getFolders(+this.projectId, this.userId).subscribe((response) => {
      this.loading = false;
      this.folders = response;

      if (this.folders.length > 1) {
        this.folders.sort((val1, val2) => {
          return (
            (new Date(val2.createdDate) as any) -
            (new Date(val1.createdDate) as any)
          );
        });
      }
    }, () => {
      this.loading = false;
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes.selectionChange.firstChange) {
      this.isVisible = true;
    }
  }

  getFiles() {
    this.selectedFileId = undefined;
    this.loading = true;
    this.sourceDataService.getFiles(+this.projectId, this.userId, this.folderId).subscribe((response) => {
      this.loading = false;
      this.files = response;

      // Sorting files list in descending order (sort by createdDate)
      if (this.files.length > 1) {
        this.files.sort((val1, val2) => {
          return (
            (new Date(val2.createdDate) as any) -
            (new Date(val1.createdDate) as any)
          );
        });
      }
      console.log("++++",response)
    }, () => {
      this.loading = false;
    });
  }

  setFolderFile(fileName, fileId) {
    this.selectedFile = fileName;
    this.selectedFileId = fileId;
  }

  cleansing() {
    localStorage.setItem('cleansing-selected-folder-name', this.folderName);
    localStorage.setItem('cleansing-selected-file-name', this.selectedFile);
    this.router.navigate([`projects/${this.projectId}/cleansing/${this.folderId}/${this.selectedFileId}`]);
  }

  public onFolderSelect(): void {
    const folder = this.folders.find(x => x.folderId === this.folderId);
    if (folder) {
      this.folderName = folder.folderName;
      this.getFiles();
    }
  }

  close(): void {
    this.selectionChange = false;
  }
}
