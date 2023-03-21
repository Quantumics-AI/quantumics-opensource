import { Component } from '@angular/core';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { FoldersService } from '../../services/folders.service';
import { Folder } from '../../models/folder';
import { Pii } from '../../models/pii';
import { Router } from '@angular/router';
import { DataService } from '../../services/data-service';

@Component({
  selector: 'app-configure-folder',
  templateUrl: './configure-folder.component.html',
  styleUrls: ['./configure-folder.component.scss']
})
export class ConfigureFolderComponent {
  private piiData: Array<Pii>;
  private folder: Folder;

  public selectedType: any;
  public fileName = 'Choose file of .csv / .txt / .xls / .xlsx format.';
  public loading: boolean;
  public error: string;
  public originalFileColumns: any;
  public currentFileColumns: any;
  public duplicateFileColumns: any;
  public isVisiablePii: boolean;

  constructor(
    private snackBar: SnackbarService,
    private router: Router,
    private folderService: FoldersService,
    private dataservice: DataService) {

    this.dataservice.updateProgress(40);
    this.folder = JSON.parse(sessionStorage.getItem('folder')) as Folder;
  }

  public validate(event) {

    this.error = this.originalFileColumns = this.currentFileColumns = '';

    if (!event.target.value) {
      this.fileName = 'Choose file of .csv / .txt / .xls / .xlsx format.';
      return;
    }

    const regex = new RegExp('(.*?).(csv|txt|xls|xlsx)');
    if (!regex.test(event.target.value.toLowerCase())) {
      event.target.value = '';
      this.snackBar.open('Please select correct file format');
      return;
    }

    const fullFileName = event.target.value?.replace(/^.*[\\\/]/, '');
    const fileName = fullFileName?.split('.').slice(0, -1).join('.');

    if (fileName.length > 100) {
      event.target.value = '';
      this.snackBar.open('File name must be less than or equal to 100 characters');
      return;
    }

    this.fileName = event.target?.files[0]?.name;
  }

  public import(event): void {

    if (!event.files.length) {
      this.snackBar.open('Please select valid file.');
      return;
    }

    this.loading = true;
    const file = event.files[0];
    const projectId = +this.folder.projectId;
    const userId = +this.folder.userId;
    const folderId = +this.folder.folderId;

    const fileExtension = file.name.split('.').pop();
    const parameter = fileExtension == 'txt' ? 'txt' : 'FU';

    this.folderService.validateFileUpload(file, projectId, userId, folderId).subscribe((response) => {
      response?.result.map(t => {
        t.Folder_Id = folderId,
          t.CSV_FILE_PATH = t.CSV_FILE_PATH,
          t.TABLE_NAME = this.folder.folderName
      });

      sessionStorage.setItem('rowPii', JSON.stringify(response?.result));
      this.router.navigate([`projects/${projectId}/ingest/pii-identification/${parameter}`]);

    }, (error) => {
      this.loading = false;

      if (!error) {
        this.snackBar.open('Erorr while uploading file. Please check with Administrator.');
      }
      else {
        if (typeof (error) === typeof (String())) {
          this.snackBar.open(error);
        } else {
          this.error = error?.ERROR_MSG;
          if (error.hasOwnProperty('ORIGINAL_COL_HEADERS')) {
            this.originalFileColumns = error?.ORIGINAL_COL_HEADERS.split(',');
          }
          if (error.hasOwnProperty('CURRENT_FILE_COL_HEADERS')) {
            this.currentFileColumns = error?.CURRENT_FILE_COL_HEADERS.split(',');
          }
          if (error.hasOwnProperty('INVALID_COLS')) {
            this.duplicateFileColumns = error?.INVALID_COLS.split(',');
          }
        }
      }
    });
  }

  public updatePiiStatus(event: any): void {
    this.piiData.find(x => x.column === event.target.value).isPII = event.target.checked;
    this.piiData.find(x => x.column === event.target.value).piiMaskActionType = "";
  }

  public upatePiiMaskingStatus(event: any, column: string): void {
    const d = this.piiData.find(x => x.column === column);
    d.piiMaskActionType = event.value;
  }
}