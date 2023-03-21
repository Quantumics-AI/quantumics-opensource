import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { RuleTypes } from '../../constants/rule-types';

@Component({
  selector: 'app-remove-duplicates',
  templateUrl: './remove-duplicates.component.html',
  styleUrls: ['./remove-duplicates.component.scss']
})
export class RemoveDuplicatesComponent implements OnDestroy, OnChanges {
  @Output() previewRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Input() columns: any;
  @Input() rule: any;

  ruleButtonLabel = 'Add';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rule && changes.rule.currentValue) {
      this.ruleButtonLabel = 'Update';
    }
  }

  saveRule(): void {
    const rule = {
      ruleInputLogic: RuleTypes.RemoveDuplicateRows,
      ruleInputLogic1: '',
      ruleInputValues: '',
      ruleOutputValues: '',
      ruleImpactedCols: [],
      numberOfPreviewColumns: 0,
      cleansingParamId: this.rule ? this.rule.cleansingParamId : 0,
      ruleSequence: this.rule ? this.rule.ruleSequence : 0,
      update: !!this.rule

    };

    this.addRule.emit(rule);
  }

  preview(): void {
    const rule = {
      ruleInputLogic: RuleTypes.RemoveDuplicateRows,
      ruleInputLogic1: '',
      ruleInputValues: '',
      ruleOutputValues: '',
      ruleImpactedCols: [],
      numberOfPreviewColumns: 0
    };

    this.previewRule.emit(rule);
  }

  cancel() {
    const reapplyRules = this.rule?.cleansingParamId > 0;
    this.cancelPreview.emit(reapplyRules);
  }

  ngOnDestroy(): void {
  }
}
