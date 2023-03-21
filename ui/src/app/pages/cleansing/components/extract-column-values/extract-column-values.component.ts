import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ExtractRuleTypes } from '../../constants/extract-rule-types';
import { getParentRuleIds } from '../../services/helpers';
import { RuleTypes } from '../../constants/rule-types';

@Component({
  selector: 'app-extract-column-values',
  templateUrl: './extract-column-values.component.html',
  styleUrls: ['./extract-column-values.component.scss']
})
export class ExtractColumnValuesComponent implements OnInit, OnChanges {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;

  options = [
    { text: 'Numbers', value: ExtractRuleTypes.Numbers },
    { text: 'Type Mismatched', value: ExtractRuleTypes.TypeMismatched },
    { text: 'First Characters', value: ExtractRuleTypes.FirstCharacters },
    { text: 'Last Characters', value: ExtractRuleTypes.LastCharacters },
    { text: 'Characters Between Positions', value: ExtractRuleTypes.CharactersBetweenPostions },
    { text: 'Text Or Pattern', value: ExtractRuleTypes.TextOrPattern },
    { text: 'Query strings', value: ExtractRuleTypes.QueryStrings },
    { text: 'Between Delimiters', value: ExtractRuleTypes.BetweenDelimiters },
  ];

  private controls: { [type: string]: Array<string>; } = {};

  @Input() public ruleInputLogic1 = ExtractRuleTypes.Numbers;
  public ruleInputValues: string;
  public ruleInputValues1: string;
  public ruleInputValues2: string;
  public ruleInputValues3: string;
  public ruleImpactedCols: string;
  public filterTypes = ExtractRuleTypes;
  public submitted = false;
  public dataTypes = [
    { text: 'Integer', value: 'number' },
    { text: 'String', value: 'string' },
    { text: 'Boolean', value: 'boolean' }
  ];

  public ruleButtonLabel = 'Add';

  public fg: FormGroup;

  constructor(private formBuilder: FormBuilder) {
    this.controls[ExtractRuleTypes.Numbers] = ['numbersToExtract'];
    this.controls[ExtractRuleTypes.QueryStrings] = ['fieldsToExtract'];
    this.controls[ExtractRuleTypes.TypeMismatched] = ['typeToMatchAgainst'];
    this.controls[ExtractRuleTypes.FirstCharacters] = ['charactersToExtract'];
    this.controls[ExtractRuleTypes.LastCharacters] = ['charactersToExtract'];
    this.controls[ExtractRuleTypes.CharactersBetweenPostions] = ['startValue', 'endValue'];
    this.controls[ExtractRuleTypes.TextOrPattern] = ['textToExtract'];
    this.controls[ExtractRuleTypes.BetweenDelimiters] = ['startingDelimiter', 'endingDelimiter', 'numberOfMatches'];
  }

  ngOnInit(): void {
    this.fg = this.formBuilder.group({
      ruleInputLogic1: new FormControl(this.ruleInputLogic1, [Validators.required]),
      ruleImpactedCols: ['', Validators.required],
      numbersToExtract: [],
      fieldsToExtract: [],
      charactersToExtract: [],
      typeToMatchAgainst: [],
      textOrPatternMatches: [],
      startValue: [],
      endValue: [],
      textToExtract: [],
      extractAfter: [],
      endExtractBefore: [],
      numberOfMatches: [],
      startingDelimiter: [],
      endingDelimiter: [],
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
      this.fg.get(control).setValidators(Validators.required);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      this.ruleButtonLabel = 'Update';
      this.ruleInputLogic1 = this.rule.ruleInputLogic1;
      this.ruleImpactedCols = this.rule.ruleImpactedCols;
      this.ruleInputValues = this.rule.ruleInputValues;
      this.ruleInputValues1 = this.rule.ruleInputValues1;
      this.ruleInputValues2 = this.rule.ruleInputValues2;
      this.ruleInputValues3 = this.rule.ruleInputValues3;
    }
  }

  get f() { return this.fg.controls; }

  saveRule(): void {
    this.submitted = true;
    const rule = {
      cleansingParamId: this.rule ? this.rule.cleansingParamId : 0,
      ruleSequence: this.rule ? this.rule.ruleSequence : 0,
      update: !!this.rule,
      ruleInputNewcolumns: this.getPreviewColumnNames().reverse().join(',')
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

  private getRuleObject(): any {
    return {
      ruleImpactedCols: [this.ruleImpactedCols],
      ruleInputLogic: RuleTypes.ExtractColumnValues,
      ruleInputLogic1: this.ruleInputLogic1,
      ruleInputValues: this.ruleInputValues,
      ruleInputValues1: this.ruleInputValues1,
      ruleInputValues2: this.ruleInputValues2,
      ruleInputValues3: this.ruleInputValues3,
      parentRuleIds: getParentRuleIds(this.columns, [this.ruleImpactedCols])
    };
  }

  public cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

  public change(evt: any): void {
    this.preview();
  }

  private get numberOfPreviewColumns(): number {
    switch (this.ruleInputLogic1) {
      case ExtractRuleTypes.Numbers:
        return +this.ruleInputValues;
      case ExtractRuleTypes.TextOrPattern:
        return +this.ruleInputValues3;
      case ExtractRuleTypes.BetweenDelimiters:
        return +this.ruleInputValues2;
      case ExtractRuleTypes.QueryStrings:
        return this.ruleInputValues?.split(',').length;
      default:
        return 1;
    }
  }

  private getPreviewColumnNames(): Array<string> {
    const startIndex = this.existingPreviewColumns(this.ruleImpactedCols);
    const previewColumns = this.numberOfPreviewColumns + startIndex - 1;
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
}

