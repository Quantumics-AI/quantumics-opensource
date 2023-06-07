import { Component, OnInit,Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { SourceDataService } from '../../services/source-data.service';

@Component({
  selector: 'app-delete-folder-dataset',
  templateUrl: './delete-folder-dataset.component.html',
  styleUrls: ['./delete-folder-dataset.component.scss']
})
export class DeleteFolderDatasetComponent implements OnInit {
  
  @Input() projectId: number;
  @Input() userId: number;
  @Input() fileId: number;
  @Input() folderId: number;
  @Input() source: string;
  constructor(
    public modal: NgbActiveModal,
    private sourceDataService: SourceDataService,
    private snakbar: SnackbarService,
  ) { }

  ngOnInit(): void {
  }

  public deleteFolder(): void {
    this.modal.close();
    // this.sourceDataService.deleteFile(this.projectId, this.userId, this.folderId, this.fileId).subscribe((response: any) => {
    //   this.snakbar.open(response.message);
    //   if (response.code === 200) {
    //     this.modal.close(response.result);
    //   }
    // }, (error) => {
    //   this.snakbar.open(error);
    // });
  }

}
