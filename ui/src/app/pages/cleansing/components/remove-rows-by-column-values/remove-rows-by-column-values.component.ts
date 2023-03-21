import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators, ValidatorFn } from '@angular/forms';
import { getParentRuleIds } from '../../services/helpers';
import { FilterRuleTypes } from '../../constants/filter-rule-types';
import { RuleTypes } from '../../constants/rule-types';

const rangeValidator: ValidatorFn = (fg: FormGroup) => {
  const ruleType = fg.get('ruleInputLogic1').value;
  const start = fg.get('startValue').value;
  const end = fg.get('endValue').value;
  const endControl = fg.get('endValue');

  if ((start !== null && end !== null && start < end) || ruleType !== FilterRuleTypes.IsBetween) {
    endControl.setErrors(null);
  } else {
    endControl.setErrors({ invalid: true });
  }

  return null;
};

@Component({
  selector: 'app-remove-rows-by-column-values',
  templateUrl: './remove-rows-by-column-values.component.html',
  styleUrls: ['./remove-rows-by-column-values.component.scss']
})
export class RemoveRowsByColumnValuesComponent implements OnInit, OnChanges {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;

  private controls: { [type: string]: Array<string>; } = {};

  public options = [
    { text: 'Is Missing', value: FilterRuleTypes.IsMissing },
    { text: 'Type Mismatched', value: FilterRuleTypes.TypeMismatched },
    { text: 'Is Exactly', value: FilterRuleTypes.EqualTo },
    { text: 'Not Equal to', value: FilterRuleTypes.NotEqualTo },
    { text: 'Is one of', value: FilterRuleTypes.IsOneOf },
    { text: 'Less than or equal to', value: FilterRuleTypes.LessThanOrEqualTo },
    { text: 'Greater than or equal to', value: FilterRuleTypes.GreaterThanOrEqualTo },

    { text: 'Is between', value: FilterRuleTypes.IsBetween },
    { text: 'Contains', value: FilterRuleTypes.Contains },
    { text: 'Start with', value: FilterRuleTypes.StartsWith },
    { text: 'End with', value: FilterRuleTypes.EndsWith }
  ];

  public filterTypes = FilterRuleTypes;
  @Input() ruleInputLogic1 = FilterRuleTypes.IsMissing;
  public ruleInputValues: string;
  public ruleInputValues1: string;
  public ruleInputValues5: 'Keep' | 'Delete' = 'Keep';
  public ruleImpactedCols: string;
  public ruleButtonLabel = 'Add';
  public values: Array<{ value: string }> = [];
  public submitted = false;
  public fg: FormGroup;

  constructor(private formBuilder: FormBuilder) {
    this.controls[FilterRuleTypes.IsMissing] = [];
    this.controls[FilterRuleTypes.IsOneOf] = [];
    this.controls[FilterRuleTypes.TypeMismatched] = [];
    this.controls[FilterRuleTypes.EqualTo] = ['equalTo'];
    this.controls[FilterRuleTypes.NotEqualTo] = ['notEqualTo'];
    this.controls[FilterRuleTypes.IsBetween] = ['startValue', 'endValue'];
    this.controls[FilterRuleTypes.GreaterThanOrEqualTo] = ['greaterThanOrEqualTo'];
    this.controls[FilterRuleTypes.LessThanOrEqualTo] = ['lessThanOrEqualTo'];
    this.controls[FilterRuleTypes.Contains] = ['contains'];
    this.controls[FilterRuleTypes.EndsWith] = ['endsWith'];
    this.controls[FilterRuleTypes.StartsWith] = ['startsWith'];
  }

  ngOnInit(): void {
    this.fg = this.formBuilder.group({
      ruleInputLogic1: new FormControl(this.ruleInputLogic1, [Validators.required]),
      ruleImpactedCols: ['', Validators.required],
      equalTo: [''],
      notEqualTo: [''],
      lessThanOrEqualTo: [''],
      greaterThanOrEqualTo: [''],
      contains: [''],
      startsWith: [''],
      endsWith: [''],
      startValue: [''],
      endValue: ['']
    }, { validator: rangeValidator });

    this.fg.get('ruleInputLogic1').valueChanges.subscribe((selectedRule) => {
      this.values.length = 0;
      this.clearValidations();
      this.setValidations(selectedRule);
      if (selectedRule === FilterRuleTypes.IsOneOf) {
        this.values.push({ value: '' });
      }
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
      this.fg.get(control).setValidators(Validators.required);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      this.ruleButtonLabel = 'Update';
      this.ruleImpactedCols = this.rule.ruleImpactedCols;
      this.ruleInputLogic1 = this.rule.ruleInputLogic1;

      this.ruleInputValues1 = this.rule.ruleInputValues1;
      this.ruleInputValues5 = this.rule.ruleInputValues5;

      if (this.ruleInputLogic1 === FilterRuleTypes.IsOneOf) {
        const values = this.rule.ruleInputValues.split(',');

        setTimeout(() => {
          this.values = values.map(value => ({ value }));
        }, 0);
      } else {
        this.ruleInputValues = this.rule.ruleInputValues;
      }
    }
  }

  get f() { return this.fg.controls; }

  saveRule(): void {
    this.submitted = true;

    const rule = {
      cleansingParamId: this.rule ? this.rule.cleansingParamId : 0,
      ruleSequence: this.rule ? this.rule.ruleSequence : 0,
      update: !!this.rule,
    };

    const rule1 = this.getRuleObject();

    this.addRule.emit(Object.assign({}, rule, rule1));

  }

  preview(): void {
    if (!this.fg.valid) {
      return;
    }

    const rule = this.getRuleObject();
    this.previewRule.emit(rule);
  }

  private getRuleObject(): any {
    return {
      ruleImpactedCols: [this.ruleImpactedCols],
      ruleInputLogic: RuleTypes.FilterRowsByColumn,
      ruleInputLogic1: this.ruleInputLogic1,
      ruleInputValues: this.getRuleInputValues(),
      ruleInputValues1: this.ruleInputValues1,
      ruleInputValues5: this.ruleInputValues5,
      parentRuleIds: getParentRuleIds(this.columns, [this.ruleImpactedCols])
    };
  }

  private getRuleInputValues(): string {
    if (this.ruleInputLogic1 === this.filterTypes.IsOneOf) {
      return this.values.map(v => v.value).join(',');
    } else {
      return this.ruleInputValues;
    }
  }

  public change(evt: any): void {
    this.preview();
  }

  public cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

  public setAction(action: 'Keep' | 'Delete'): void {
    this.ruleInputValues5 = action;
    this.preview();
  }

  public addValue(): void {
    this.values.push({ value: '' });
  }

  public removeValue(index: number): void {
    if (this.values.length !== 1) {
      this.values.splice(index, 1);
      this.preview();
    }
  }
}
