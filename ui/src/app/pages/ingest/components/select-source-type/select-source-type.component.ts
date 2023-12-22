import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { FoldersService } from '../../services/folders.service';
import { Observable } from 'rxjs';
import { ConnectDbComponent } from '../connect-db/connect-db.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConnectToApiComponent } from '../connect-to-api/connect-to-api.component';
import { ImportLocalFileComponent } from '../import-local-file/import-local-file.component';

@Component({
  selector: 'app-select-source-type',
  templateUrl: './select-source-type.component.html',
  styleUrls: ['./select-source-type.component.scss']
})
export class SelectSourceTypeComponent implements OnInit {
  projectId: number;
  sourceTypes$: Observable<any>;
  searchBy: string;
  folderName: string;
  projectName: string;
  type: 'all' | 'crm' | 'db' = 'all';


  constructor(
    private router: Router,
    private location: Location,
    private activatedRoute: ActivatedRoute,
    private foldersService: FoldersService,
    private modalService: NgbModal) { }

  ngOnInit(): void {
    this.projectId = parseInt(this.activatedRoute.snapshot.paramMap.get('projectId'), 10);
    this.folderName = this.activatedRoute.snapshot.paramMap.get('folderName');
    this.projectName = localStorage.getItem('projectname');
    this.sourceTypes$ = this.foldersService.getSourceTypes();
    sessionStorage.clear();
  }

  public back(): void {
    this.location.back();
  }

  public handleSelection(dataSourceName: string, type: 'crm' | 'db' | 'localfile'): void {
    switch (type.toLowerCase()) {
      case 'db':
        // this.openDatabaseConnector(dataSourceName);
        this.redirectToCreateDb()
        break;
      case 'api':
        this.openAPIConnector();
        break;
      case 'localfile':
        // this.openFileUploader();
        this.redirectToCreateFolder();
        break;

      default:
        break;
    }

  }


  // Do not remove this method
  private openFileUploader(): void {
    const modalRef = this.modalService.open(ImportLocalFileComponent, { size: 'lg' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.result.then((result) => {
      console.log(result);
    }, (reason) => {
      console.log(reason);
    });
  }

  private redirectToCreateFolder(): void {
    this.router.navigate([`projects/${this.projectId}/ingest/local-file`]);
  }

  public redirectToCloud(): void {
    this.router.navigate([`projects/${this.projectId}/ingest/generate-aws`]);
  }

  private redirectToCreateDb(): void {
    this.router.navigate([`projects/${this.projectId}/ingest/db-connector`]);
  }

  private openAPIConnector(): void {
    const modalRef = this.modalService.open(ConnectToApiComponent, { size: 'lg' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.result.then((result) => {
      console.log(result);
    }, (reason) => {
      console.log(reason);
    });
  }

  private openDatabaseConnector(sourceType: string): void {
    const modalRef = this.modalService.open(ConnectDbComponent, { size: 'lg', scrollable: true });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.sourceType = sourceType;

    modalRef.result.then((result) => {
      console.log(result);
    }, (reason) => {
      console.log(reason);
    });
  }
}
