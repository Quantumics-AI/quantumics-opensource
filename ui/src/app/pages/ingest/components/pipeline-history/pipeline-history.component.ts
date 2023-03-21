import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { DbConnectorService } from '../../services/db-connector.service';

@Component({
  selector: 'app-pipeline-history',
  templateUrl: './pipeline-history.component.html',
  styleUrls: ['./pipeline-history.component.scss']
})
export class PipelineHistoryComponent implements OnInit {
  loading: boolean;
  projectId: number;
  userId: number;
  pipelineId: number;
  pipelineData: any;

  constructor(
    public modal: NgbActiveModal,
    private snakbar: SnackbarService,
    private sourceDataService: DbConnectorService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    console.log("Project id", this.projectId + "user id ", this.userId + "pipeline id ", this.pipelineId);
    this.pipelineFileData();
  }

  pipelineFileData() {
    this.loading = true;
    this.sourceDataService.getPipelineFileData(+this.projectId, +this.userId, +this.pipelineId).subscribe((res) => {
      this.loading = false;
      this.pipelineData = res.result;
      this.snakbar.open(res.message);
      
    })
  }

  public viewLogs(history: any): void {
    localStorage.setItem('batchJobLog', history?.pipelineLog);
    this.router.navigate([]).then(() => { window.open(`/projects/${this.projectId}/automation/logs`, '_blank'); });
  }

  public refresh(): void {
    this.pipelineFileData();
  }
}
