import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { ActivatedRoute } from '@angular/router';
import {UdfData} from '../../models/project';
import {ProjectsService} from '../../services/projects.service';

@Component({
  selector: 'app-update-udf',
  templateUrl: './update-udf.component.html',
  styleUrls: ['./update-udf.component.scss']
})
export class UpdateUdfComponent implements OnInit {

  userId: number;
  projectId: number;

  public loading: boolean;

  public selectedDataType: string;
  public selectedOptions: string;
  public selectedDataValue: string;
  public selectedLanguage: string;

  @Input() udfData: UdfData;

  private unsubscribe: Subject<void> = new Subject<void>();

  public fg: FormGroup;

  constructor(
    private fb: FormBuilder,
    private projectsService: ProjectsService,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private snakbar: SnackbarService,
    public modal: NgbActiveModal,
  ) { 
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    // this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      udfname: new FormControl(this.udfData?.udfName, Validators.required),
      udfdefination: new FormControl(this.udfData?.udfScript, Validators.required),
      dataValue: new FormControl(this.udfData?.arguments, Validators.required),
      dataType: new FormControl(this.udfData?.arguments, Validators.required),
      options: new FormControl(this.udfData?.arguments, Validators.required),
      scriptLanguage: new FormControl(this.udfData?.udfScriptLanguage, Validators.required),
      udfsyntax: new FormControl(this.udfData?.udfSyntax, Validators.required),
    });
    console.log(this.udfData);
    
  }

  updateUdf(){
    const udfData = {
      projectId: this.projectId,
      userId: this.userId,
      udfName: this.fg.controls.udfname.value,
      udfScript: this.fg.controls.udfdefination.value,
      udfVersion: "1.0",
      udfIconName:"Test",
      udfReturnvalue: this.fg.controls.udfReturn.value,
      // arguments: this.fg.controls.dataValue.value + "," + this.fg.controls.dataType.value + "," + this.fg.controls.options.value,
      udfFilePath: "S3 location should be updated",
      udfScriptLanguage: this.fg.controls.scriptLanguage.value,
      udfSyntax: this.fg.controls.udfsyntax.value,
      logicfile: this.fg.get('logicFile').value,
      publish: this.fg.controls.publish.value,
      arguments: this.fg.controls.itemRows.value,
      Active: true
    } as UdfData

    this.projectsService.updateUdf(udfData).subscribe((response) => {
      this.loading = false;
      this.modal.close();
      if (response.code === 200) {

      }

      this.snakbar.open(response.message);
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });
  }

}
