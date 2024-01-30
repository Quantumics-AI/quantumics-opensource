import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

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
    public modal: NgbActiveModal
  ) { }

  ngOnInit(): void {
  }

  public deleteFolder(): void {
    this.modal.close();
  }

}
