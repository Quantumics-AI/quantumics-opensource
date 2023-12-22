import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { DataQuailtyListResponse } from '../../models/data-quality-list-response';
import { ActivatedRoute, Router } from '@angular/router';


@Component({
  selector: 'app-data-quality-edit',
  templateUrl: './data-quality-edit.component.html',
  styleUrls: ['./data-quality-edit.component.scss']
})
export class DataQualityEditComponent {
  public fg: FormGroup;
  public sourceTargetType: string = 'target';
  public isShowData: boolean = false;

  private projectId: number;

  constructor(private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly activatedRoute: ActivatedRoute) {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
  }

  ngOnInit(): void {
    const dataQuailty = JSON.parse(sessionStorage.getItem('editDataQuality')) as DataQuailtyListResponse;

    if (!dataQuailty) {
      this.router.navigate([`projects/${this.projectId}/data-quality`]);
      return;
    }

    this.fg = this.fb.group({
      ruleName: new FormControl(dataQuailty?.ruleName, Validators.required),
      ruleDescription: new FormControl(dataQuailty?.ruleDescription, Validators.required),
      type: ['target'],
      sourceDataSource: new FormControl('aws'),
      sourceDataConnection: new FormControl('aws1'),
      sourceBucket: new FormControl('bucket1'),
      sourceFolder: new FormControl('folder1'),

      targetDataSource: new FormControl('aws', Validators.required),
      targetDataConnection: new FormControl('aws1', Validators.required),
      targetBucket: new FormControl('bucket1', Validators.required),
      targetFolder: new FormControl('folder1', Validators.required),
    });
  }

  public selectedType(type: string): void {
    if (type === 'sourceAndTarget') {
      this.fg.get('sourceDataSource').setValidators([Validators.required]);
      this.fg.get('sourceDataConnection').setValidators([Validators.required]);
      this.fg.get('sourceBucket').setValidators([Validators.required]);
      this.fg.get('sourceFolder').setValidators([Validators.required]);
    } else {
      this.fg.get('sourceDataSource')?.clearValidators();
      this.fg.get('sourceDataConnection')?.clearValidators();
      this.fg.get('sourceBucket')?.clearValidators();
      this.fg.get('sourceFolder')?.clearValidators();
    }

    this.fg.get('sourceDataSource')?.updateValueAndValidity();
    this.fg.get('sourceDataConnection')?.updateValueAndValidity();
    this.fg.get('sourceBucket')?.updateValueAndValidity();
    this.fg.get('sourceFolder')?.updateValueAndValidity();
  }

  public continue(): void {

  }

  public showData(): void {
    this.isShowData = !this.isShowData;
    if (!this.fg.valid) {
      return;
    }

  }
}
