import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { SourceDataService } from '../../services/source-data.service';

@Component({
  selector: 'app-folder-confirmation',
  templateUrl: './folder-confirmation.component.html',
  styleUrls: ['./folder-confirmation.component.scss']
})
export class FolderConfirmationComponent implements OnInit {

  @Input() projectId: number;
  @Input() userId: number;
  @Input() folderId: number;

  constructor(
    public modal: NgbActiveModal,
    private sourceDataService: SourceDataService,
    private snakbar: SnackbarService,
  ) { }

  ngOnInit(): void {
  }

  public deleteFolder(): void {
    this.modal.close();
    // this.sourceDataService.deleteFolder(this.projectId, this.userId, this.folderId).subscribe((response: any) => {
    //   this.snakbar.open(response.message);
    //   if (response.code === 200) {
    //     this.modal.close(response.result);
    //   }
    // }, (error) => {
    //   this.snakbar.open(error);
    // });
  }

}
