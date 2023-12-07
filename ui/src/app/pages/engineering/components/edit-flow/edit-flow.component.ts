import { Component, Input, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { EngineeringService } from '../../services/engineering.service';

@Component({
  selector: 'app-edit-flow',
  templateUrl: './edit-flow.component.html',
  styleUrls: ['./edit-flow.component.scss']
})
export class EditFlowComponent implements OnInit {

  @Input() flow: any;
  fg: FormGroup;

  public loading: boolean;

  constructor(
    public modal: NgbActiveModal,
    private fb: FormBuilder,
    private snakbar: SnackbarService,
    private engService: EngineeringService) {
  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      flowName: new FormControl(this.flow?.engFlowDisplayName, [Validators.required, Validators.maxLength(20), Validators.pattern(/^([A-Za-z0-9_]).([A-Za-z0-9_]+\s)*[A-Za-z0-9_]+$/)]),
      description: new FormControl(this.flow?.engFlowDesc, Validators.max(255))
    });
  }

  edit(): void {
    const flow = {
      engFlowDesc: this.fg.value.description,
      engFlowMetaData: '{}',
      engFlowName: this.fg.value.flowName,
      parentEngFlowId: 0,
      projectId: this.flow.projectId,
      userId: this.flow.userId,
      engFlowId: this.flow.engFlowId
    };

    this.loading = true;

    this.engService.editFlow(flow).subscribe((response) => {
      this.loading = false;
      this.snakbar.open(`${response.message}`);
      if (response.code === 200) {
        this.modal.close({
          engFlowDesc: this.fg.value.description,
          engFlowName: this.fg.value.flowName,
          engFlowId: this.flow.engFlowId
        });
      }
    }, (error) => {
      this.modal.close(`${error}`);
      this.loading = false;
    });
  }
}
