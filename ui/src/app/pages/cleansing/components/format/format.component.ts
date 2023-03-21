import { Component, OnInit, Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Rule } from '../../models/rule';
import { FormatRuleTypes } from '../../constants/format-rule-types';

import { getParentRuleIds } from '../../services/helpers';

@Component({
  selector: 'app-format',
  templateUrl: './format.component.html',
  styleUrls: ['./format.component.scss']
})

export class FormatComponent implements OnInit, OnChanges {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: Rule;

  selectedColumns: string;
  public submitted = false;
  public form: FormGroup;
  public formatRuletypes = FormatRuleTypes;
  @Input() ruleInputLogic1: string;
  public ruleInputValues: string;
  public ruleInputValues1: string;

  ruleButtonLabel = 'Add';

  constructor(private formBuilder: FormBuilder) { }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      column: ['', Validators.required],
      formatOption: [this.ruleInputLogic1, Validators.required],
      textToAdd: [''],
      charactersToPadd: [''],
      paddLength: [0]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      this.selectedColumns = this.rule.ruleImpactedCols;
      this.ruleInputLogic1 = this.rule.ruleInputLogic1;
      this.ruleInputValues = this.rule.ruleInputValues;
      this.ruleInputValues1 = this.rule.ruleInputValues1;
      this.ruleButtonLabel = 'Update';
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

  private getRuleObject(): any {
    const cols = [this.selectedColumns];
    const d = {
      ruleImpactedCols: cols,
      ruleInputLogic: 'format',
      ruleInputLogic1: this.ruleInputLogic1,
      ruleInputValues: this.ruleInputValues ?? '',
      ruleInputValues1: this.ruleInputValues1 ?? 0,
      parentRuleIds: getParentRuleIds(this.columns, cols),
      numberOfPreviewColumns: 1
    };
    return d;
  }

  preview(): void {
    if (this.form.invalid) {
      return;
    }
    const rule = this.getRuleObject();
    this.previewRule.emit(rule);
  }

  public cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

  public change(evt: any): void {
    this.preview();
  }
}
