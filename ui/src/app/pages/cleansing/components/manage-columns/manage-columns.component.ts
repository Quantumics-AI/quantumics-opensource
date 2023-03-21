import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators } from '@angular/forms';
import { getParentRuleIds } from '../../services/helpers';
import { ManageColumnRuleTypes } from '../../constants/manage-column-rule-types';
import { RuleTypes } from '../../constants/rule-types';
import { CleansingDataService } from '../../services/cleansing-data.service';
import { ActivatedRoute } from '@angular/router';
import { Certificate } from 'src/app/models/certificate';
import { Observable } from 'rxjs/internal/Observable';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-manage-columns',
  templateUrl: './manage-columns.component.html',
  styleUrls: ['./manage-columns.component.scss']
})
export class ManageColumnsComponent implements OnInit, OnChanges, OnDestroy {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;

  public submitted = false;
  public options = [
    { text: 'Add new column', value: ManageColumnRuleTypes.Create },
    // { text: 'Remove column', value: ManageColumnRuleTypes.Drop },
    { text: 'Clone column', value: ManageColumnRuleTypes.Clone },
    // { text: 'Rename column', value: ManageColumnRuleTypes.Rename },
    { text: 'Data Type Cast', value: ManageColumnRuleTypes.DataTypeCast },
  ];

  public invalidDataTypeCast = false;

  private controls: { [type: string]: Array<string>; } = {};

  public ruleImpactedCols: string;
  public ruleButtonLabel = 'Add';
  @Input() public ruleInputLogic1 = ManageColumnRuleTypes.Create;
  public ruleInputValues: string;
  public ruleInputValues1: string;

  public ruletypes = ManageColumnRuleTypes;

  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject();

  fg: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private cleansingDataService: CleansingDataService,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private snackBar: SnackbarService) {
    this.controls[ManageColumnRuleTypes.Create] = ['ruleInputValues', 'ruleInputValues1'];
    this.controls[ManageColumnRuleTypes.Drop] = [];
    this.controls[ManageColumnRuleTypes.Clone] = [];
    this.controls[ManageColumnRuleTypes.Rename] = ['ruleInputValues'];
    this.controls[ManageColumnRuleTypes.DataTypeCast] = ['ruleInputValues'];

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
      });
  }

  ngOnInit(): void {
    this.fg = this.formBuilder.group({
      ruleInputLogic1: new FormControl(this.ruleInputLogic1, [Validators.required]),
      ruleImpactedCols: ['', Validators.required],
      ruleInputValues1: ['', Validators.required],
      ruleInputValues: ['', [Validators.required, this.columnNameValidator]],
    });

    this.fg.get('ruleInputLogic1').valueChanges.subscribe((selectedRule) => {
      this.clearValidations();
      this.setValidations(selectedRule);
    });
  }

  private clearValidations(): void {
    const controlsArray: Array<Array<string>> = Object.values(this.controls);
    const controls = [].concat.apply([], controlsArray);
    for (const control of controls) {
      this.fg.get(control)?.setErrors(null);
      this.fg.get(control)?.reset();
      this.fg.get(control)?.clearValidators();
      this.fg.get(control)?.updateValueAndValidity();
    }
  }

  private setValidations(selectedRule: string): void {
    const controls = this.controls[selectedRule];
    for (const control of controls) {
      if (control === 'ruleInputValues') {
        this.fg.get(control).setValidators([Validators.required, this.columnNameValidator]);
      } else {
        this.fg.get(control).setValidators(Validators.required);
      }
    }
  }

  private columnNameValidator = (control: FormControl) => {
    const isDuplicateName = this.columns.some(c => c.column_name.toLowerCase() === control.value?.toLowerCase());
    return isDuplicateName ? { unique: true } : null;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      this.ruleButtonLabel = 'Update';
      this.ruleInputLogic1 = this.rule.ruleInputLogic1;
      this.ruleImpactedCols = this.rule.ruleImpactedCols;
      this.ruleInputValues = this.rule.ruleInputValues;
      this.ruleInputValues1 = this.rule.ruleInputValues1;
    }
  }

  get f() { return this.fg.controls; }

  saveRule(): void {
    this.submitted = true;
    const rule = {
      cleansingParamId: this.rule ? this.rule.cleansingParamId : 0,
      ruleSequence: this.rule ? this.rule.ruleSequence : 0,
      update: !!this.rule
    };

    const rule1 = this.getRuleObject();
    if (rule1.ruleInputLogic1 === ManageColumnRuleTypes.DataTypeCast) {
      const d = {
        projectId: +this.activatedRoute.snapshot.paramMap.get('projectId'),
        folderId: +this.activatedRoute.snapshot.paramMap.get('folderId'),
        fileId: +this.activatedRoute.snapshot.paramMap.get('fileId'),
        userId: +this.certificateData.user_id,
        columnName: this.ruleImpactedCols,
        targetDataType: this.ruleInputValues,
      };

      this.cleansingDataService.validateDataTypeCastRule(d).subscribe((res) => {
        if (res.result === 'True') {
          this.addRule.emit(Object.assign({}, rule, rule1));
        } else {
          this.snackBar.open(res.message);
          this.submitted = false;
        }
      });

    } else {
      this.addRule.emit(Object.assign({}, rule, rule1));
    }
  }

  preview(): void {
    if (this.fg.invalid) {
      return;
    }

    const rule = this.getRuleObject();

    this.previewRule.emit(rule);
  }

  private getRuleObject(): any {
    const d = {
      ruleImpactedCols: [this.ruleImpactedCols],
      ruleInputLogic: RuleTypes.ManageColumns,
      ruleInputLogic1: this.ruleInputLogic1,
      ruleInputValues: this.ruleInputValues,
      ruleInputValues1: this.ruleInputValues1,
      parentRuleIds: getParentRuleIds(this.columns, [this.ruleImpactedCols])
    };

    return d;
  }

  public cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

  public change(evt: any): void {
    this.preview();
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}

