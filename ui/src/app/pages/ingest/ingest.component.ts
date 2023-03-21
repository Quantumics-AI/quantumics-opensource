import { Component, OnInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { SourceDataService } from './services/source-data.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-ingest',
  templateUrl: './ingest.component.html',
  styleUrls: ['./ingest.component.scss']
})
export class IngestComponent implements OnInit {
  folders: any;
  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject();
  projectId: string;
  projectName: string;
  userId: number;
  loading: boolean;
  editFolder: boolean;
  folder: any;

  constructor(
    private router: Router,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private sourceDataService: SourceDataService,
    private snakbar: SnackbarService) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        if (certificate) {
          this.certificateData = certificate;
          this.userId = +this.certificateData.user_id;
        }
      });
  }

  ngOnInit(): void {
    this.projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
    this.projectName = localStorage.getItem('projectname');
    this.foldersList();
  }

  create(folder: any): void {
    this.loading = true;
    folder.userId = this.certificateData.user_id;
    folder.projectId = this.projectId;
    folder.dataOwner = this.certificateData.user;

    this.sourceDataService.createFolder(folder).subscribe((response) => {
      this.loading = false;
      this.snakbar.open(response.message);
      if (response.code === 200) {
        this.foldersList();
      }
    }, (error) => {
      this.snakbar.open(error);
      this.loading = false;
    });
  }

  edit(folder: any): void {
    this.folder = folder;
    this.editFolder = true;
  }

  updatedFolder(folder: any): void {
    const index = this.folders.findIndex(x => x.folderId === folder?.folderId);
    this.folders[index] = folder;
  }

  delete(folder: any): void {
    this.loading = true;
    this.sourceDataService.deleteFolder(+this.projectId, +this.certificateData.user_id, folder.folderId).subscribe((res) => {
      this.loading = false;
      if (res?.code === 200) {
        const idx = this.folders.findIndex(x => x.folderId === folder.folderId);
        this.folders.splice(idx, 1);
        this.snakbar.open(res?.message);
      }
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });
  }

  foldersList() {
    this.loading = true;
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
}
