import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators } from '@angular/forms';
import { FilterRuleTypes } from '../../constants/filter-rule-types';
import { RuleTypes } from '../../constants/rule-types';

@Component({
  selector: 'app-remove-rows',
  templateUrl: './remove-rows.component.html',
  styleUrls: ['./remove-rows.component.scss']
})
export class RemoveRowsComponent implements OnInit, OnChanges {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;

  options = [
    { text: 'Top rows', value: FilterRuleTypes.TopRows },
    { text: 'Range', value: FilterRuleTypes.Range },
    { text: 'At regular interval', value: FilterRuleTypes.RegularInterval }
  ];
  private controls: { [type: string]: Array<string>; } = {};
  @Input() public ruleInputLogic1 = FilterRuleTypes.TopRows;
  public ruleInputValues: string;
  public ruleInputValues1: string;

  public ruleInputValues4: 'default' | 'columns' = 'default';
  public ruleButtonLabel = 'Add';
  public ruleInputValues5: 'Keep' | 'Delete' = 'Keep';
  public sortColumns = [];

  fg: FormGroup;
  public submitted = false;
  public filterTypes = FilterRuleTypes;

  constructor(private formBuilder: FormBuilder) {
    this.controls[FilterRuleTypes.TopRows] = ['topRows'];
    this.controls[FilterRuleTypes.Range] = ['startRow', 'endRow'];
    this.controls[FilterRuleTypes.RegularInterval] = ['interval', 'iStartRow'];
  }

  ngOnInit(): void {
    this.fg = this.formBuilder.group({
      ruleInputLogic1: [this.ruleInputLogic1, Validators.required],
      ruleInputValues4: [this.ruleInputValues4, Validators.required],
      topRows: ['', Validators.required],
      startRow: ['', Validators.required],
      endRow: ['', Validators.required],
      interval: ['', Validators.required],
      iStartRow: ['', Validators.required],
    }, {
      validator: this.rangeValidator('startRow', 'endRow'),
    });

    this.fg.get('ruleInputLogic1').valueChanges.subscribe((selectedRule) => {
      this.clearValidations();
      this.setValidations(selectedRule);
    });
  }

  private getRuleObject(): any {
    return {
      ruleImpactedCols: this.getRuleImpactedCols(),
      ruleInputLogic: RuleTypes.FilterRows,
      ruleInputLogic1: this.ruleInputLogic1,
      ruleInputValues: this.ruleInputValues,
      ruleInputValues1: this.ruleInputValues1,
      ruleInputValues4: this.ruleInputValues4,
      ruleInputValues5: this.ruleInputValues5,
    };
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
      this.ruleInputLogic1 = this.rule.ruleInputLogic1;
      this.ruleInputValues = this.rule.ruleInputValues;
      this.ruleInputValues1 = this.rule.ruleInputValues1;
      this.ruleInputValues5 = this.rule.ruleInputValues5;
      this.ruleInputValues4 = this.rule.ruleInputValues4;

      if (this.ruleInputValues4 === 'columns') {
        this.setRuleImpactedCols();
      }
    }

  }

  get f() { return this.fg.controls; }

  private setRuleImpactedCols(): void {
    const columns = this.rule.ruleImpactedCols.split(',');
    this.sortColumns = columns;
  }

  private getRuleImpactedCols(): any {
    if (this.ruleInputValues4 === 'columns') {
      return this.sortColumns;
    } else {
      return [];
    }
  }

  saveRule(): void {
    this.submitted = true;
    const rule = {
      cleansingParamId: this.rule ? this.rule.cleansingParamId : 0,
      ruleSequence: this.rule ? this.rule.ruleSequence : 0,
      update: !!this.rule
    };

    const rule1 = this.getRuleObject();

    this.addRule.emit(Object.assign({}, rule, rule1));
  }

  preview(): void {

    if (this.fg.invalid) {
      return;
    }

    const rule = this.getRuleObject();
    this.previewRule.emit(rule);
  }

  cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

  setAction(action: 'Keep' | 'Delete'): void {
    this.ruleInputValues5 = action;
    this.preview();
  }

  handleSortChange(): void {
    if (this.ruleInputValues4 === 'default') {
      this.sortColumns.length = 0;
    } else {
      this.addColumn();
    }

    this.preview();
  }

  public addColumn(): void {
    const col = this.columns[0];
    this.sortColumns.push(col.column_name);
  }

  public removeColumn(index: number): void {
    if (this.sortColumns.length !== 1) {
      this.sortColumns.splice(index, 1);
      this.preview();
    }
  }

  private rangeValidator(a, b): any {
    return (formGroup: FormGroup) => {
      if (this.ruleInputLogic1 !== FilterRuleTypes.Range) {
        return;
      }

      const startControl = formGroup.controls[a];
      const endControl = formGroup.controls[b];

      if (endControl.errors && !endControl.errors.rangeValidator) {
        return;
      }
      if (+startControl.value > +endControl.value) {
        endControl.setErrors({ rangeValidator: true });
      } else {
        endControl.setErrors(null);
      }
    };
  }
}
