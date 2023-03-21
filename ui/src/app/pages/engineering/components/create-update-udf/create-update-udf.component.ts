import { Component, Input, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Arguments, UdfRequestModel } from '../../models/udfRequestModel';
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
      values: this.fb.array([])
    });
  }

  ngOnInit(): void {
    if (this.isUpdate) {
      this.udfForm.controls.udfName.setValue(this.udf.udfName);
      this.udfForm.controls.language.setValue(this.udf.udfScriptLanguage);
      this.udfForm.controls.dataType.setValue(this.udf.udfReturnvalue);
      this.udfForm.controls.udfSyntax.setValue(this.udf.udfSyntax);
      this.udfForm.controls.udfscript.setValue(this.udf.udfScript);

      if (this.udf.arguments) {
        for (const arg of this.udf.arguments) {
          const form = this.newValues();
          form.setValue(arg);
          this.values().push(form);
        }
      }
    }
    else if (!this.values().length) {
      this.addNewValues();
    }

    console.log(this.udf);

  }

  newValues(): FormGroup {
    return this.fb.group({
      dataValue: [''],
      options: [''],
      dataType: [''],
      parameter: ['']
    });
  }

  values(): FormArray {
    return this.udfForm.get("values") as FormArray;
  }

  addNewValues(): void {
    this.values().push(this.newValues());
  }

  public remove(index: number): void {
    this.values().removeAt(index);
  }

  public create(): void {
    if (this.udfForm.invalid) {
      alert('Please fill the required fields');
      return;
    }

    let udfArgument: Arguments[];
    udfArgument = this.udfForm.controls.values.value;
    const noOfArgs = udfArgument.filter(x => x.dataValue === 'Data Frame');

    if (noOfArgs.length > 5) {
      alert('Maximum 5 Data Frame is allowed');
      return;
    }

    const request = {
      projectId: this.projectId,
      userId: this.userId,
      udfName: this.udfForm.controls.udfName.value,
      arguments: udfArgument,
      udfReturnvalue: this.udfForm.controls.dataType.value,
      udfScriptLanguage: this.udfForm.controls.language.value,
      udfSyntax: this.udfForm.controls.udfSyntax.value,
      udfVersion: '1.0', // need to confirm the version
      udfScript: this.udfForm.controls.udfscript.value,
      udfFilePath: 'S3 location should be updated',
      udfIconName: 'Test',
      logicfile: ''

    } as UdfRequestModel;

    if (this.isUpdate) {
      this.udfService.update(request)
        .pipe(takeUntil(this.unsubscribe))
        .subscribe((response: any) => {
          if (response.code === 200) {
            this.modal.close();
          }
          this.snackbar.open(`${response.message}`);
        }, (error) => {
          this.snackbar.open(`${error}`);
        })
    } else {
      this.udfService.save(request)
        .pipe(takeUntil(this.unsubscribe))
        .subscribe((response: any) => {
          if (response.code === 200) {
            this.modal.close();
          }

          this.snackbar.open(`${response.message}`);
        }, (error) => {
          this.snackbar.open(`${error}`);
        });
    }
  }

  checkValue(event: any) {
    console.log(event);
  }

}
