import { Component, OnInit } from '@angular/core';
import { Input, Output, EventEmitter, SimpleChanges, OnChanges } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { CountMatchRuleTypes } from '../../constants/count-match-rule-types';
import { RuleTypes } from '../../constants/rule-types';
import { getParentRuleIds } from '../../services/helpers';


@Component({
  selector: 'app-count-matches',
  templateUrl: './count-matches.component.html',
  styleUrls: ['./count-matches.component.scss']
})

export class CountMatchesComponent implements OnInit, OnChanges {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;

  public submitted = false;

  public ruleImpactedCols: string;
  public ruleButtonLabel = 'Add';
  public fg: FormGroup;
  public ruleInputValues: string;
  public ruleInputValues1: string;
  public ruleInputValues2: string;
  public ruleInputLogic1 = CountMatchRuleTypes.Text;
  private controls: { [type: string]: Array<string>; } = {};

  constructor(private formBuilder: FormBuilder) {
    this.controls[CountMatchRuleTypes.Text] = ['ruleInputValues'];
    this.controls[CountMatchRuleTypes.Delimiter] = ['ruleInputValues', 'ruleInputValues1'];
  }

  ngOnInit(): void {
    this.fg = this.formBuilder.group({
      ruleImpactedCols: ['', Validators.required],
      ruleInputLogic1: [this.ruleInputLogic1, Validators.required],
      ruleInputValues: [this.ruleInputValues, Validators.required],
      ruleInputValues1: [this.ruleInputValues1, Validators.required],
      ruleInputValues2: [this.ruleInputValues2, [Validators.required, this.columnNameValidator]],
    });

    this.fg.get('ruleInputLogic1').valueChanges.subscribe((selectedRule) => {
      this.clearValidations();
      this.setValidations(selectedRule);
    });
  }

  private columnNameValidator = (control: FormControl) => {
    const isDuplicateName = this.columns.some(c => c.column_name.toLowerCase() === control.value?.toLowerCase());
    return isDuplicateName ? { unique: true } : null;
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
      this.fg.get(control).setValidators(Validators.required);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      this.ruleImpactedCols = this.rule.ruleImpactedCols;
      this.ruleInputValues = this.rule.ruleInputValues;
      this.ruleInputValues1 = this.rule.ruleInputValues1;
      this.ruleInputValues2 = this.rule.ruleInputValues2;
      this.ruleInputLogic1 = this.rule.ruleInputLogic1;
      this.ruleButtonLabel = 'Update';
    }
  }

  saveRule() {
    this.submitted = true;

    const rule = {
      cleansingParamId: this.rule ? this.rule.cleansingParamId : 0,
      ruleSequence: this.rule ? this.rule.ruleSequence : 0,
      update: !!this.rule,
      parentRuleIds: getParentRuleIds(this.columns, [this.ruleImpactedCols])
    };

    const rule1 = this.getRuleObject();
    this.addRule.emit(Object.assign({}, rule, rule1));
  }

  preview(): void {
    if (this.fg.invalid) {
      return;
    }

    const d = this.getRuleObject();

    this.previewRule.emit(d);
  }

  private getRuleObject(): any {
    return {
      ruleImpactedCols: [this.ruleImpactedCols],
      ruleInputLogic: RuleTypes.CountMatch,
      ruleInputLogic1: this.ruleInputLogic1,
      ruleInputValues: this.ruleInputValues,
      ruleInputValues1: this.ruleInputValues1,
      ruleInputValues2: this.ruleInputValues2,
    };
  }

  public change(evt: any): void {
    this.preview();
  }

  public cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

}
