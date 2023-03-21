import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { AutomationService } from '../../services/automation.service';

@Component({
  selector: 'app-cleansing-select-files',
  templateUrl: './cleansing-select-files.component.html',
  styleUrls: ['./cleansing-select-files.component.scss']
})
export class CleansingSelectFilesComponent implements OnInit, OnChanges {
  @Input() projectId: number;
  @Input() userId: number;
  @Input() folderId: number;

  files$: Observable<any>;
  selectedFileId: number;
  loading: boolean;
  isVisible = true;

  constructor(
    public modal: NgbActiveModal,
    private automationService: AutomationService,
    private snackBar: SnackbarService
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes.folderId.firstChange) {
      this.isVisible = true;
      this.getFiles();
    }
  }

  ngOnInit(): void {
    this.getFiles();
  }

  getFiles() {
    this.loading = true;
    this.files$ = this.automationService.getFiles(this.projectId, this.userId, this.folderId).pipe(
      map((res: any) => {
        this.loading = false;
        if (res.code === 200) {
          console.log(res.result);

          // Sorting file list in descending order (sort by createdDate)
         
          res.result.sort((val1, val2) => {
            return (
              (new Date(val2.createdDate) as any) -
              (new Date(val1.createdDate) as any)
            );
          });
          
          return res.result;
        } else {
          return [];
        }
      }, (error) => {
        this.loading = false;
      })
    );
  }

  selectFile(evnt: any, fileId: number): void {
    if (evnt.target.checked) {
      this.selectedFileId = fileId;
    } else {
      this.selectedFileId = undefined;
    }
  }

  runCleanseJob() {
    this.loading = true;
    const params = {
      projectId: this.projectId,
      fileId: this.selectedFileId
    };

    this.automationService.runJob(params).subscribe((response: any) => {
      this.loading = false;
      this.snackBar.open(response.message);
      this.modal.dismiss();
    }, (error) => {
      this.loading = false;
    });
  }
}
