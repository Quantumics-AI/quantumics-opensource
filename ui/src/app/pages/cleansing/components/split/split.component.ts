import { Component, OnInit, Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormArray, FormControl } from '@angular/forms';
import { getParentRuleIds } from '../../services/helpers';
import { SplitRuleTypes } from '../../constants/split-rule-types';
import { RuleTypes } from '../../constants/rule-types';

@Component({
  selector: 'app-split',
  templateUrl: './split.component.html',
  styleUrls: ['./split.component.scss']
})
export class SplitComponent implements OnInit, OnChanges {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;
  @Input() ruleInputLogic1 = SplitRuleTypes.ByDelimiter;

  private controls: { [type: string]: Array<string>; } = {};
  public ruleImpactedCols: string;
  public ruleInputValues: string;
  public ruleInputValues1: string;
  public ruleButtonLabel = 'Add';
  public fg: FormGroup;
  public splitRuleTypes = SplitRuleTypes;
  public validSequence = true;
  public submitted = false;

  splitOptions = [
    { text: 'By delimiter', value: SplitRuleTypes.ByDelimiter },
    { text: 'Between delimiter', value: SplitRuleTypes.BetweenDelimiter },
    // { text: 'Multiple delimiter', value: SplitRuleTypes.MultipleDelimiter },
    { text: 'By positions', value: SplitRuleTypes.ByPositions },
    // { text: 'At regular intervals', value: SplitRuleTypes.AtRegularIntervals },
    { text: 'Between two positions', value: SplitRuleTypes.BetweenTwoPositions }

  ];

  constructor(private formBuilder: FormBuilder) {
    this.controls[SplitRuleTypes.ByDelimiter] = ['delimiter'];
    this.controls[SplitRuleTypes.BetweenDelimiter] = ['delimiter1', 'delimiter2'];
    this.controls[SplitRuleTypes.ByPositions] = [];
    this.controls[SplitRuleTypes.BetweenTwoPositions] = ['startPosition', 'endPosition'];
  }

  ngOnInit(): void {
    this.fg = this.formBuilder.group({
      ruleImpactedCols: ['', Validators.required],
      ruleInputLogic1: [this.ruleInputLogic1, Validators.required],
      delimiter: [],
      delimiter1: [],
      delimiter2: [],
      startPosition: [],
      endPosition: []
    });

    this.fg.get('ruleInputLogic1').valueChanges.subscribe((selectedRule) => {
      this.clearValidations();
      this.setValidations(selectedRule);
    });
  }

  get positionsControls() {
    return this.fg.get('positions') as FormArray;
  }

  addPosition(value = ''): void {
    const control: any = this.fg.controls.positions;
    const position = this.formBuilder.group({
      position: [value, [Validators.required, this.minimumValidator]]
    });

    control.push(position);
  }

  removePosition(i: number): void {
    const controls: any = this.fg.controls.positions;
    if (controls?.length !== 1) {
      controls.removeAt(i);
      this.preview();
    }
  }

  private initializePotions(value = '') {
    return this.formBuilder.group({
      position: [value, [Validators.required, this.minimumValidator]]
    });
  }

  private minimumValidator = (control: FormControl) => {
    const position = control?.value ?? 0;
    return 1 > position ? { lessThanOne: true } : null;
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

    this.fg.removeControl('positions');
  }

  private setValidations(selectedRule: string): void {
    const controls = this.controls[selectedRule];
    for (const control of controls) {
      this.fg.get(control).setValidators(Validators.required);
    }

    if (selectedRule === SplitRuleTypes.ByPositions) {
      this.fg.addControl('positions', this.formBuilder.array([
        this.initializePotions()
      ]));
    }
  }

  private getRuleInputValues(): string {
    switch (this.ruleInputLogic1) {
      case SplitRuleTypes.ByPositions:
        const formArray = this.fg.controls.positions as FormArray;
        const positions = formArray.controls.map(c => c.value.position).join(',');
        return positions;
      default:
        return this.ruleInputValues;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      this.ruleImpactedCols = this.rule.ruleImpactedCols;
      this.ruleButtonLabel = 'Update';
      this.ruleInputLogic1 = this.rule.ruleInputLogic1;
      this.ruleInputValues1 = this.rule.ruleInputValues1;

      if (this.ruleInputLogic1 === SplitRuleTypes.ByPositions) {
        const values = this.rule.ruleInputValues.split(',').map(v => +v);

        setTimeout(() => {
          const formArray = this.fg.controls.positions as FormArray;
          const first = values.shift();
          const ctrl = formArray.controls[0];
          ctrl.setValue({ position: first });

          this.fg.addControl('positions', this.formBuilder.array([
            this.initializePotions(first)
          ]));

          for (const v of values) {
            this.addPosition(v);
          }
        }, 100);
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
      parentRuleIds: getParentRuleIds(this.columns, [this.ruleImpactedCols]),
      ruleInputNewcolumns: this.getPreviewColumnNames().reverse().join(',')
    };

    const rule1 = this.getRuleObject();

    this.addRule.emit(Object.assign({}, rule, rule1));
  }


  private getPreviewColumnNames(): Array<string> {
    const startIndex = this.existingPreviewColumns(this.ruleImpactedCols);
    const previewColumns = this.previewColumns + startIndex - 1;
    const newColumns: Array<string> = [];
    for (let i = previewColumns; i >= startIndex; i--) {
      const previewColumn = `${this.ruleImpactedCols}${i}`;
      newColumns.push(previewColumn);
    }

    return newColumns;
  }


  private existingPreviewColumns(column: string): number {
    const regEx = new RegExp(`^${column}\\d+$`);
    const endingNumbersRegEx = /\d+$/;

    const previewColumns = this.columns.filter(c => regEx.test(c.column_name) && c.preview !== 'new').map(c => {
      const matches = c.column_name.match(endingNumbersRegEx);
      if (matches) {
        return parseInt(matches[0], 10);
      } else {
        return 1;
      }
    });

    if (previewColumns.length) {
      return Math.max.apply(Math, previewColumns) + 1;
    } else {
      return 1;
    }
  }


  private get previewColumns(): number {
    if (this.ruleInputLogic1 === SplitRuleTypes.ByPositions) {
      const ri = this.getRuleInputValues();
      return ri.split(',').length + 1;
    }

    // return default value
    return 2;
  }

  preview(): void {
    if (this.fg.invalid) {
      return;
    }

    const rule = this.getRuleObject();

    if (rule.ruleInputLogic1 === SplitRuleTypes.ByPositions) {
      this.validSequence = this.validateSequence(rule.ruleInputValues);
    } else {
      this.validSequence = true;
    }

    this.previewRule.emit(rule);
  }

  private validateSequence(valuesString: string) {
    const values = valuesString.split(',');
    return values.every((num, i) => i === values.length - 1 || +num < +values[i + 1]);
  }

  private getRuleObject(): any {
    return {
      ruleImpactedCols: [this.ruleImpactedCols],
      ruleInputLogic: RuleTypes.SplitColumnValue,
      ruleInputLogic1: this.ruleInputLogic1,
      ruleInputValues: this.getRuleInputValues(),
      ruleInputValues1: this.ruleInputValues1
    };
  }

  public cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

  public change(evt: any): void {
    this.preview();
  }
}
