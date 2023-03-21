import { Component, OnInit,Input } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, FormArray } from '@angular/forms';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Router, ActivatedRoute } from '@angular/router';
import {UdfData} from '../../models/project';
import {ProjectsService} from '../../services/projects.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-create-udf',
  templateUrl: './create-udf.component.html',
  styleUrls: ['./create-udf.component.scss']
})
export class CreateUdfComponent implements OnInit {

  userId: number;
  projectId: number;

  public loading: boolean;

  public selectedDataType: string;
  public selectedOptions: string;
  public selectedDataValue: string;
  public selectedLanguage: string;
  public logicFileName: string;
  // public logicFile: any;
  public publish = false;

  @Input() udfData: UdfData;

  private unsubscribe: Subject<void> = new Subject<void>();


  // dataarray: [];
  public fg: FormGroup;

  constructor(
    private fb: FormBuilder,
    private projectsService: ProjectsService,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private snakbar: SnackbarService,
    private router: Router,
  ) { 

    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);

  }

  ngOnInit(): void {

    this.logicFileName = "Upload logic file of .py format"
    this.fg = this.fb.group({
      udfname: new FormControl(this.udfData?.udfName, Validators.required),
      udfdefination: new FormControl(this.udfData?.udfScript, Validators.required),
      scriptLanguage: new FormControl(this.udfData?.udfScriptLanguage, Validators.required),
      udfsyntax: new FormControl(this.udfData?.udfSyntax, Validators.required),
      udfReturn: new FormControl(this.udfData?.udfReturnvalue, Validators.required),
      publish: [this.publish, [Validators.required]],
      logicFile: [''],
      arguments: this.fb.array([this.initItemRows()]),
    });
    
  }

  get formArr() {
    return this.fg.get('arguments') as FormArray;
  }

  initItemRows() {
    return this.fb.group({
      dataValue: new FormControl(this.udfData?.arguments, Validators.required),
      options: new FormControl(this.udfData?.arguments, Validators.required),
      dataType: new FormControl(this.udfData?.arguments, Validators.required),
     
    });
  }

  public validate(event) {

    if (!event.target.value) {
      this.logicFileName = 'Upload logic file of .py format';
      return;
    }

    const regex = new RegExp('(.*?).(py)$');
    if (!regex.test(event.target.value.toLowerCase())) {
      event.target.value = '';
      this.snakbar.open('Please select correct file format');
      return;
    }
    const file = event.target?.files[0];
    this.fg.get('logicFile').setValue(file);
    // this.logicFile = event.target?.files[0];
    this.logicFileName = event.target?.files[0]?.name;
  }

  addNewRow() {
    this.formArr.push(this.initItemRows());
  }

  deleteRow(index: number) {
    this.formArr.removeAt(index);
  }

  createUdfFunction(): void{
    this.loading = true;
    
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
      arguments: this.fg.controls.arguments.value,
      Active: true
    } as UdfData
    console.log(JSON.stringify(udfData));


    this.projectsService.createUdf(udfData).subscribe((response) => {
      this.loading = false;

      if (response.code === 200) {

      }

      this.snakbar.open(response.message);
      this.router.navigate([`projects/${this.projectId}/engineering-udf/edit-udf`]);
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });
    
  }


  clearValues() {
    this.fg.reset()
  }

}