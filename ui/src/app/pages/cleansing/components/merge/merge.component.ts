import { Component, OnInit, Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl, ValidationErrors } from '@angular/forms';
import { getParentRuleIds } from '../../services/helpers';
import { RuleTypes } from '../../constants/rule-types';

@Component({
  selector: 'app-merge',
  templateUrl: './merge.component.html',
  styleUrls: ['./merge.component.scss']
})
export class MergeComponent implements OnInit, OnChanges {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;

  selectionType = ColumnSelectionType;
  ruleInputValues1: any;
  selectionTypes: any;
  ruleButtonLabel = 'Add';
  form: FormGroup;
  ruleInputValues2: string;
  ruleInputValues3: string;
  public submitted = false;
  isError = false;



  public ruleInputValues: string;
  public ruleDelimiter: string;
  public ruleImpactedCols: any = [];

  constructor(private formBuilder: FormBuilder) {
    this.setSelectionTypes();
  }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      ruleInputValues1: [this.selectionType.Multiple],
      ruleImpactedCols: [''],
      ruleInputValues2: [''],
      ruleInputValues3: [''],
      ruleDelimiter: ['', Validators.required],
      ruleInputValues: ['', [Validators.required, this.columnNameValidator]],
    });

    this.form.get('ruleInputValues1').valueChanges.subscribe((selectedRule) => {

      this.form.get('ruleInputValues2')?.setErrors(null);
      this.form.get('ruleInputValues2')?.reset();
      this.form.get('ruleInputValues2')?.clearValidators();
      this.form.get('ruleInputValues2')?.updateValueAndValidity();

      this.form.get('ruleInputValues3')?.setErrors(null);
      this.form.get('ruleInputValues3')?.reset();
      this.form.get('ruleInputValues3')?.clearValidators();
      this.form.get('ruleInputValues3')?.updateValueAndValidity();

      this.form.get('ruleImpactedCols')?.setErrors(null);
      this.form.get('ruleImpactedCols')?.reset();
      this.form.get('ruleImpactedCols')?.clearValidators();
      this.form.get('ruleImpactedCols')?.updateValueAndValidity();


      if (+selectedRule === ColumnSelectionType.Range) {
        this.form.get('ruleInputValues2').setValidators(Validators.required);
        this.form.get('ruleInputValues2')?.updateValueAndValidity();

        this.form.get('ruleInputValues3').setValidators(Validators.required);
        this.form.get('ruleInputValues3')?.updateValueAndValidity();

      } else {
        this.form.get('ruleImpactedCols').setValidators(Validators.required);
        this.form.get('ruleImpactedCols')?.updateValueAndValidity();
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      const temp = this.rule.ruleImpactedCols?.split(',');
      this.ruleImpactedCols = temp;
      this.ruleButtonLabel = 'Update';
      this.ruleDelimiter = this.rule.ruleDelimiter;
      this.ruleInputValues = this.rule.ruleInputValues;
      this.ruleInputValues1 = this.rule.ruleInputValues1;
      this.ruleInputValues2 = this.rule.ruleInputValues2;
      this.ruleInputValues3 = this.rule.ruleInputValues3;
    }
  }

  private columnNameValidator = (control: FormControl) => {
    const isDuplicateName = this.columns.some(c => c.column_name.toLowerCase() === control.value?.toLowerCase());
    return isDuplicateName ? { unique: true } : null;
  }

  private setSelectionTypes(): any {
    this.selectionTypes = Object.keys(this.selectionType)
      .filter(Number)
      .map(key => ({ value: +key, title: this.selectionType[key] }));

    this.ruleInputValues1 = +this.selectionType.Multiple;
  }

  get f() { return this.form.controls; }

  saveRule(): void {
    const cols = this.getSelectedColumns();
    if (cols.length < 2) {
      this.isError = true;
      return;
    }

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
    if (this.form.invalid) {
      return;
    }

    this.isError = false;
    const cols = this.getSelectedColumns();

    if (cols.length < 2) {
      this.isError = true;
      return;
    }

    const rule = this.getRuleObject();

    this.previewRule.emit(rule);
  }

  private getRuleObject(): any {
    return {
      ruleImpactedCols: this.getSelectedColumns(),
      ruleInputLogic: RuleTypes.Merge,
      ruleInputValues: this.ruleInputValues,
      ruleInputValues1: this.ruleInputValues1,
      ruleInputValues2: this.ruleInputValues2,
      ruleInputValues3: this.ruleInputValues3,
      ruleDelimiter: this.ruleDelimiter,
      parentRuleIds: getParentRuleIds(this.columns, this.getSelectedColumns()),
    };
  }

  public cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

  getSelectedColumns(): any {
    let cols = [];
    if (+this.ruleInputValues1 === ColumnSelectionType.Multiple) {
      cols = this.ruleImpactedCols;
    } else if (+this.ruleInputValues1 === ColumnSelectionType.Range) {
      if (this.ruleInputValues2 && this.ruleInputValues3) {
        let startIndex = this.columns.findIndex(c => c.column_name === this.ruleInputValues2);
        let endIndex = this.columns.findIndex(c => c.column_name === this.ruleInputValues3);
        if (+startIndex > +endIndex) {
          const tmp = endIndex;
          endIndex = startIndex;
          startIndex = tmp;
        }
        cols = this.columns.slice(startIndex, endIndex + 1).map(c => c.column_name);
      } else {
        cols = [];
      }
    }
    return cols;
  }
}

export enum ColumnSelectionType {
  Multiple = 1,
  Range = 2
}
