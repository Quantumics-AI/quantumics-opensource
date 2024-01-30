import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { AutomationService } from '../../services/automation.service';

@Component({
  selector: 'app-select-files',
  templateUrl: './select-files.component.html',
  styleUrls: ['./select-files.component.scss']
})
export class SelectFilesComponent implements OnInit {
  userId: number;
  projectId: number;
  flowId: number;
  selectedFiles: any[];
  loading: boolean;

  public cleansedFiles: Array<any>;
  public engFiles: Array<any>;
  public rawFiles: Array<any>;

  constructor(
    public modal: NgbActiveModal,
    private automationService: AutomationService,
    private snakbar: SnackbarService
  ) {
    this.selectedFiles = [];
  }

  ngOnInit(): void {
    this.loading = true;

    this.automationService.getFilesToRun(this.projectId, this.userId, this.flowId).subscribe((res) => {
      this.loading = false;

      if (res.code === 200) {
        this.cleansedFiles = res.result.processed.map(f => ({ ...f, category: 'processed' }));
        this.engFiles = res.result.eng.map(f => ({ ...f, category: 'eng' }));
        this.rawFiles = res.result.raw.map(f => ({ ...f, category: 'raw' }));
      }
    }, (error) => {
      this.loading = false;
      this.snakbar.open('Something went wrong, please try later');
    });
  }

  selectFile(evt, file: any): void {
    const isSelected: boolean = evt.target.checked;
    if (isSelected) {
      const tempFile = {
        projectId: this.projectId,
        engFlowId: this.flowId,
        name: file.fileName,
        type: 'file',
        fileType: file.category,
        fileId: file.fileId,
        folderId: file.folderId,
        eventId: file.eventId,
      };
      this.selectedFiles.push(tempFile);
    } else {
      this.selectedFiles = this.selectedFiles.filter(f => f.eventId !== file.eventId);
    }
  }

  public runJob(): void {
    const data = {
      projectId: this.projectId,
      userId: this.userId,
      engineeringId: this.flowId,
      selectedFiles: this.selectedFiles,
      selectedJoins: [],
      selectedAggregates: [],
      selectedUdfs:[]
    };

    this.automationService.runAutomation(data).subscribe((res) => {
      this.modal.close();
      this.snakbar.open('Process started');
    });
  }

  public get rawFilesSelected(): number {
    return this.selectedFiles.filter(f => f.fileType === 'raw')?.length;
  }

  public get engFilesSelected(): number {
    return this.selectedFiles.filter(f => f.fileType === 'eng')?.length;
  }

  public get cleansedFilesSelected(): number {
    return this.selectedFiles.filter(f => f.fileType === 'processed')?.length;
  }
}
