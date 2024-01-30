import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators } from '@angular/forms';
import { RuleTypes } from '../../constants/rule-types';
import { Certificate } from 'src/app/models/certificate';
import { Observable } from 'rxjs/internal/Observable';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { RowIdRuleTypes } from '../../constants/row-Id';

@Component({
  selector: 'app-row-id',
  templateUrl: './row-id.component.html',
  styleUrls: ['./row-id.component.scss']
})

export class RowIdComponent implements OnInit, OnChanges, OnDestroy {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;

  public submitted = false;
  public options = [
    { text: 'First Column', value: RowIdRuleTypes.FirstColumn, label: RowIdRuleTypes.FirstColumn  },
    { text: 'Last Column', value: RowIdRuleTypes.LastColumn, label: RowIdRuleTypes.LastColumn },
  ];

  public invalidDataTypeCast = false;

  private controls: { [type: string]: Array<string>; } = {};

  public ruleImpactedCols: string;
  public ruleButtonLabel = 'Add';
  public startFromValues: string;
  public columnNameValues: string;

  public labelText: string;
  public selectedOption: string='';

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();

  fg: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private quantumFacade: Quantumfacade) {
    this.controls[RowIdRuleTypes.Create] = ['selectedOption' ,'startFromValues', 'columnNameValues'];

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        
      });
  }

  ngOnInit(): void {
    this.fg = this.formBuilder.group({
      selectedOption: ['', Validators.required],
      columnNameValues: ['', [Validators.required, this.columnNameValidator]],
      startFromValues: ['', Validators.required],
    });
  
  }

 
  private columnNameValidator = (control: FormControl) => {
    let isDuplicateName = this.columns.some(c => c.column_name.toLowerCase() === control.value?.toLowerCase());
    return isDuplicateName ? { unique: true } : null;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      this.ruleButtonLabel = 'Update';
      this.selectedOption = this.rule.ruleInputValues1;
      this.startFromValues = this.rule.ruleInputValues2;
      this.columnNameValues = this.rule.ruleInputValues;
      this.preview();
    }
  }

  get f() { return this.fg.controls; }

  saveRule(): void {
   
    this.submitted = true;
    const rule = {
      cleansingParamId: this.rule ? this.rule.cleansingParamId : 0,
      ruleSequence: this.rule ? this.rule.ruleSequence : 0,
      update: this.ruleButtonLabel == 'Update'? true : false,
    };
    const rule1 = this.getRuleObject();
    this.addRule.emit(Object.assign({}, rule, rule1));
  }

  preview(): void {
    if (!this.selectedOption || !this.startFromValues || !this.columnNameValues) {
      return;
    }
    const rule = this.getRuleObject();
    this.previewRule.emit(rule);
  }

  private getRuleObject(): any {
   
    const d = {
      cleansingParamId: this.rule?.cleansingParamId,
      ruleInputLogic: RuleTypes.RowId,
      ruleInputValues1: this.selectedOption,
      ruleInputValues2: this.startFromValues,
      ruleInputValues: this.columnNameValues,
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