import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-rule-types',
  templateUrl: './rule-types.component.html',
  styleUrls: ['./rule-types.component.scss']
})
export class RuleTypesComponent {
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  private certificateData: Certificate;

  projectId: string;
  userId: number;
  public fg: FormGroup;
  public queryRuleTypes: string;
  public queryDcLevel: string;
  public queryDpLevel: string;
  public queryDvLevel: string;
  public isTest: boolean = true;
  public selectedCompleteName: string;
  public completenessData: any = [
    "Salary",
		"Basic",
		"Gross",
		"Age"
  ];

  public selectedDataProfiler: string;
  public dataProfiler: any = [
    "Salary",
		"Basic",
		"Gross",
		"Age"
  ];

  public selectedDuplicateDataOne: string;
  public duplicateDataOne: any = [
    "Salary",
		"Basic",
		"Gross",
		"Age"
  ]

  public duplicateDataTwo: any = [
    "Salary",
		"Basic",
		"Gross",
		"Age"
  ]

  

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private location: Location,
    private fb: FormBuilder,
    private snakbar: SnackbarService,
  ){
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        if (certificate) {
          this.certificateData = certificate;
          this.userId = +this.certificateData.user_id;
        }
      });

      this.fg = this.fb.group({
        ruleType:[''],
        dcLevel: [''],
        dpLevel: [''],
        dvLevel: [''],
        relatedAttributes: [''],
        relatedDataProfiler: [''],
        relatedDuplicateDataOne: [''],
        relatedDuplicateDataTwo: [''],
        dcRowPercentage: [''],
        dcColumnPercentage: [''],
        dpColumnPercentage: [''],
        dpTablePercentage: [''],
        nullPercentage: [''],
        dvDuplicatePercentage: [''],
        dvColumnPercentage: [''],
        dvMultColumnPercentage: [''],

        // dvLevel: new FormControl('', Validators.required),
      });
  }

  ngOnInit(): void {
    this.projectId = localStorage.getItem('project_id');
  }

  public back(): void {
    this.location.back();
  }

  public onSelectDataCom(connectorName: any): void {

  }

  public saveRules(): void {
    this.snakbar.open("Rule saved");
    this.router.navigate([`projects/${this.projectId}/data-quality`]);
  }

  public runRules(): void {
    this.router.navigate([`projects/${this.projectId}/data-quality`]);
  }

  public onItemChange(value: string): void {
    console.log(this.queryRuleTypes);
    
  }
}
