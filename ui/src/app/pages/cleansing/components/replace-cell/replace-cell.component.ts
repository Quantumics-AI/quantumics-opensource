import { Component, OnInit } from '@angular/core';
import { Input, Output, OnDestroy, EventEmitter, SimpleChanges, OnChanges } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { RuleTypes } from '../../constants/rule-types';
import { getParentRuleIds } from '../../services/helpers';

@Component({
  selector: 'app-replace-cell',
  templateUrl: './replace-cell.component.html',
  styleUrls: ['./replace-cell.component.scss']
})

export class ReplaceCellComponent implements OnInit, OnChanges, OnDestroy {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;
  selectedColumns: string;
  ruleButtonLabel = 'Add';
  fg: FormGroup;
  findstring: string;
  replacestring: string;
  public submitted = false;
  private unsubscribe: Subject<void> = new Subject();

  constructor(private formBuilder: FormBuilder) { }

  ngOnInit(): void {
    this.fg = this.formBuilder.group({
      column: ['', Validators.required],
      findString: [this.findstring, Validators.required],
      replaceString: [this.replacestring, Validators.required],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      this.selectedColumns = this.rule.ruleImpactedCols;
      this.findstring = this.rule.ruleInputValues;
      this.replacestring = this.rule.ruleInputValues1;
      this.ruleButtonLabel = 'Update';
    }
  }

  get f() { return this.fg.controls; }

  saveRule(): void {
    this.submitted = true;
    const cols = [this.selectedColumns];

    const rule = {
      ruleImpactedCols: cols,
      ruleInputValues: this.fg.value.findString,
      ruleInputValues1: this.fg.value.replaceString,
      ruleInputLogic: RuleTypes.FindReplace,
      cleansingParamId: this.rule ? this.rule.cleansingParamId : 0,
      ruleSequence: this.rule ? this.rule.ruleSequence : 0,
      update: !!this.rule,
      parentRuleIds: getParentRuleIds(this.columns, cols)
    };

    this.addRule.emit(rule);
  }

  preview(): void {
    const cols = [this.selectedColumns];

    const d = {
      ruleInputLogic: RuleTypes.FindReplace,
      ruleInputValues: this.fg.value.findString,
      ruleInputValues1: this.fg.value.replaceString,
      ruleImpactedCols: cols,
    };

    this.previewRule.emit(d);
  }

  public change(evt: any): void {
    this.preview();
  }

  public cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
