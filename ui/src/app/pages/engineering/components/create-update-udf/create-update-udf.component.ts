import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { UdfRequestModel } from '../../models/udfRequestModel';
import { UdfService } from '../../services/udf.service';

@Component({
  selector: 'app-create-update-udf',
  templateUrl: './create-update-udf.component.html',
  styleUrls: ['./create-update-udf.component.scss']
})
export class CreateUpdateUdfComponent implements OnInit {

  @Input() projectId: number;
  @Input() userId: number;
  @Input() isUpdate: boolean;
  @Input() udf: UdfRequestModel;

  public udfForm: FormGroup;
  public isUploadLogic: boolean = false;
  public loading: boolean;

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private udfService: UdfService,
    private fb: FormBuilder,
    private snackbar: SnackbarService,
    public modal: NgbActiveModal) {
    this.udfForm = this.fb.group({
      udfName: ['', Validators.required],
      language: ['', Validators.required],
      dataType: ['', Validators.required],
      udfSyntax: ['', Validators.required],
      udfscript: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    if (this.isUpdate) {
      this.udfForm.controls.udfName.setValue(this.udf.udfName);
      this.udfForm.controls.language.setValue(this.udf.udfScriptLanguage);
      this.udfForm.controls.dataType.setValue(this.udf.udfReturnvalue);
      this.udfForm.controls.udfSyntax.setValue(this.udf.udfSyntax);
      this.udfForm.controls.udfscript.setValue(this.udf.udfScript);
    }
  }

  public create(): void {
    if (this.udfForm.invalid) {
      this.snackbar.open('Please fill the required fields');
      return;
    }

    const request = {
      projectId: this.projectId,
      userId: this.userId,
      udfName: this.udfForm.controls.udfName.value,
      arguments: [],
      udfReturnvalue: this.udfForm.controls.dataType.value,
      udfScriptLanguage: this.udfForm.controls.language.value,
      udfSyntax: this.udfForm.controls.udfSyntax.value,
      udfVersion: '1.0', // need to confirm the version
      udfScript: this.udfForm.controls.udfscript.value,
      udfFilePath: 'S3 location should be updated',
      udfIconName: 'Test',
      logicfile: ''

    } as UdfRequestModel;

    this.loading = true;

    if (this.isUpdate) {
      this.udfService.update(request)
        .pipe(takeUntil(this.unsubscribe))
        .subscribe((response: any) => {
          this.loading = false;
          if (response.code === 200) {
            this.modal.close(true);
          }
          this.snackbar.open(`${response.message}`);
        }, (error) => {
          this.loading = false;
          this.snackbar.open(`${error}`);
        })
    } else {
      this.udfService.save(request)
        .pipe(takeUntil(this.unsubscribe))
        .subscribe((response: any) => {
          this.loading = false;
          if (response.code === 200) {
            this.modal.close(true);
          }

          this.snackbar.open(`${response.message}`);
        }, (error) => {
          this.loading = false;
          this.snackbar.open(`${error}`);
        });
    }
  }

  checkValue(event: any) {
    console.log(event);
  }
}
