import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { DbConnectorService } from '../../services/db-connector.service';

@Component({
  selector: 'app-import-db',
  templateUrl: './import-db.component.html',
  styleUrls: ['./import-db.component.scss']
})
export class ImportDbComponent implements OnInit {
  projectId: number;
  userId: number;
  folderId: number;
  pipelineId: number;
  history$: Observable<any>;
  loading: boolean;
  fg: FormGroup;
  executePipelines: any

  constructor(
    private router: Router,
    public modal: NgbActiveModal,
    private snakbar: SnackbarService,
    private sourceDataService: DbConnectorService,
  ) { }

  ngOnInit(): void {
    this.executePipelineData();
  }

  executePipelineData() {
    this.loading = true;
    this.sourceDataService.getPipelineExecuteData(+this.projectId, +this.userId, +this.pipelineId).subscribe((res) => {
      this.loading = false;
      this.executePipelines = res.result;
      this.snakbar.open(res.message);
      
    })
  }

}
