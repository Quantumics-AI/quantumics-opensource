import { Component, OnInit, Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { getParentRuleIds } from '../../services/helpers';
import { ReplaceMissingRuleTypes } from '../../constants/replace-missing-rule-types';
import { RuleTypes } from '../../constants/rule-types';

@Component({
  selector: 'app-replace-missing',
  templateUrl: './replace-missing.component.html',
  styleUrls: ['./replace-missing.component.scss']
})

export class ReplaceMissingComponent implements OnInit, OnChanges {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;

  selectionType = ColumnSelectionType;
  selectionTypes: any;
  public ruleInputValues = 1;
  public fg: FormGroup;
  public ruleInputLogic1 = ReplaceMissingRuleTypes.CustomValue;
  public ruleTypes = ReplaceMissingRuleTypes;
  public ruleInputValues1: number;
  public ruleInputValues2: number;
  public ruleInputValues3: string;
  public submitted = false;

  replaceOptions = [
    { text: 'Custom value', value: ReplaceMissingRuleTypes.CustomValue },
    { text: 'Fill down with last valid value', value: ReplaceMissingRuleTypes.LastValidValue },
    { text: 'Null', value: ReplaceMissingRuleTypes.Null },
    { text: 'Average', value: ReplaceMissingRuleTypes.Average },
    { text: 'Mode', value: ReplaceMissingRuleTypes.Mod },
    { text: 'Sum', value: ReplaceMissingRuleTypes.Sum }
  ];

  ruleButtonLabel = 'Add';
  ruleImpactedCols: string;

  constructor(
    private formBuilder: FormBuilder) {
    this.setSelectionTypes();
  }

  ngOnInit(): void {
    this.fg = this.formBuilder.group({
      ruleImpactedCols: ['', Validators.required],
      ruleInputLogic1: [this.ruleInputLogic1],
      ruleInputValues1: [''],
      ruleInputValues2: [''],
      ruleInputValues3: ['', Validators.required],
      ruleInputValues: [this.selectionType.Multiple]
    });

    this.fg.get('ruleInputValues').valueChanges.subscribe((selectedRule) => {
      const controls = ['ruleImpactedCols', 'ruleInputValues1', 'ruleInputValues2'];
      for (const control of controls) {
        this.fg.get(control)?.setErrors(null);
        this.fg.get(control)?.reset();
        this.fg.get(control)?.clearValidators();
        this.fg.get(control)?.updateValueAndValidity();
      }

      if (+selectedRule === ColumnSelectionType.Multiple) {
        this.fg.get('ruleImpactedCols').setValidators(Validators.required);
      } else if (+selectedRule === ColumnSelectionType.Range) {
        this.fg.get('ruleInputValues1').setValidators(Validators.required);
        this.fg.get('ruleInputValues2').setValidators(Validators.required);
      }

    });

    this.fg.get('ruleInputLogic1').valueChanges.subscribe((selectedRule) => {
      if (selectedRule === ReplaceMissingRuleTypes.CustomValue) {
        this.fg.get('ruleInputValues1').setValidators(Validators.required);
      } else {
        this.fg.get('ruleInputValues3')?.setErrors(null);
        this.fg.get('ruleInputValues3')?.reset();
        this.fg.get('ruleInputValues3')?.clearValidators();
        this.fg.get('ruleInputValues3')?.updateValueAndValidity();
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {

      this.ruleImpactedCols = this.rule.ruleImpactedCols;

      this.ruleInputLogic1 = this.rule.ruleInputLogic1;
      this.ruleInputValues = this.rule.ruleInputValues;
      this.ruleInputValues1 = this.rule.ruleInputValues1;
      this.ruleInputValues2 = this.rule.ruleInputValues2;
      this.ruleInputValues3 = this.rule.ruleInputValues3;
      this.ruleButtonLabel = 'Update';
    }
  }

  saveRule() {
    const cols = this.getSelectedColumns();
    if (cols.length === 0) {
      return;
    }

    this.submitted = true;

    const rule = {
      cleansingParamId: this.rule ? this.rule.cleansingParamId : 0,
      ruleSequence: this.rule ? this.rule.ruleSequence : 0,
      update: !!this.rule,
      parentRuleIds: getParentRuleIds(this.columns, cols)
    };

    const rule1 = this.getRuleObject();

    this.addRule.emit(Object.assign({}, rule, rule1));

  }

  public change(evt: any): void {
    this.preview();
  }

  public cancel() {
    const cols = this.getSelectedColumns();
    this.cancelPreview.emit(cols);
  }

  private setSelectionTypes(): any {
    this.selectionTypes = Object.keys(this.selectionType)
      .filter(Number)
      .map(key => ({ value: +key, title: this.selectionType[key] }));

    this.ruleInputValues = +this.selectionType.Multiple;
  }

  getSelectedColumns(): any {
    let cols = [];
    if (+this.ruleInputValues === ColumnSelectionType.Multiple) {
      cols = [this.ruleImpactedCols];
    } else if (+this.ruleInputValues === ColumnSelectionType.Range) {
      if (this.ruleInputValues1 && this.ruleInputValues2) {
        let startIndex = +this.ruleInputValues1;
        let endIndex = +this.ruleInputValues2;
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

  preview(): void {
    if (this.fg.invalid) {
      return;
    }

    const rule = this.getRuleObject();
    this.previewRule.emit(rule);
  }

  private getRuleObject(): any {
    const cols = this.getSelectedColumns();
    return {
      ruleImpactedCols: cols,
      ruleInputLogic: RuleTypes.ReplaceNull,
      ruleInputLogic1: this.ruleInputLogic1,
      ruleInputLogic2: '',
      ruleInputLogic3: '',
      ruleInputLogic4: '',
      ruleInputLogic5: '',
      ruleInputValues: this.ruleInputValues,
      ruleInputValues1: this.ruleInputValues1 ?? '',
      ruleInputValues2: this.ruleInputValues2 ?? '',
      ruleInputValues3: this.ruleInputValues3 ?? '',
      ruleInputValues4: '',
      ruleInputValues5: '',
      ruleOutputValues: ''
    };
  }
}

export enum ColumnSelectionType {
  Multiple = 1,
  Range = 2
}
