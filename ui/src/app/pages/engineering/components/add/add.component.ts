import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-add',
  templateUrl: './add.component.html',
  styleUrls: ['./add.component.scss']
})
export class AddComponent {
  fg: FormGroup;

  constructor(
    public modal: NgbActiveModal,
    private fb: FormBuilder) {
    this.fg = this.fb.group({
      flowName: ['', [Validators.required, Validators.maxLength(30), Validators.pattern(/^([A-Za-z0-9_]).([A-Za-z0-9_]+\s)*[A-Za-z0-9_]+$/)]],
      description: ['', Validators.max(255)]
    });
  }

  save(): void {
    const flow = {
      engFlowDesc: this.fg.value.description,
      engFlowMetaData: '{}',
      engFlowName: this.fg.value.flowName,
      parentEngFlowId: 0,
    };

    this.modal.close(flow);
  }

}
