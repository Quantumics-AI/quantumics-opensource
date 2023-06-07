import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { DbConnectorService } from '../../services/db-connector.service';

@Component({
  selector: 'app-pipeline-confirmation',
  templateUrl: './pipeline-confirmation.component.html',
  styleUrls: ['./pipeline-confirmation.component.scss']
})
export class PipelineConfirmationComponent implements OnInit {

  @Input() projectId: number;
  @Input() userId: number;
  @Input() pipelineId: number;

  constructor(
    public modal: NgbActiveModal,
    private sourceDataService: DbConnectorService,
    private snakbar: SnackbarService,
  ) { }

  ngOnInit(): void {
  }

  public deleteFolder(): void {
    this.modal.close();
    // this.sourceDataService.deletePipeline(this.projectId, this.userId, this.pipelineId).subscribe((response: any) => {
    //   this.snakbar.open(response.massage);
    //   if (response.code === 200) {
    //     this.modal.close(response.result);
    //   }
    // }, (error) => {
    //   this.snakbar.open(error);
    // });
  }

}
