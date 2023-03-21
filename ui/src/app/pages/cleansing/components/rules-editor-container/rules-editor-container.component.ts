import { EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Component } from '@angular/core';
import { RuleTypes } from '../../constants/rule-types';

@Component({
  selector: 'app-rules-editor-container',
  templateUrl: './rules-editor-container.component.html',
  styleUrls: ['./rules-editor-container.component.scss']
})
export class RulesEditorContainerComponent implements OnChanges {
  rules: any;
  @Input() selectedRule: any;
  @Input() columns: any;
  @Input() editorComponent: string;
  @Input() ruleInputLogic1: string;
  @Input() piiColumns: Array<string>;

  @Output() previewRule = new EventEmitter<any>();
  @Output() addRule = new EventEmitter<any>();
  @Output() cancelPreview = new EventEmitter<any>();

  public ruleTypes = RuleTypes;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.columns) {
      this.columns = this.columns.filter(c => c.preview !== 'new');
      this.columns.forEach(col => {
        col.disabled = this.piiColumns.includes(col.column_name);
      });
    }
  }

  handleAddRule(rule: any): void {
    this.addRule.emit(rule);
  }

  handlePreviewRule(rule: any): void {
    this.previewRule.emit(rule);
  }

  handleCancelPreview(reapplyRules: boolean): void {
    this.cancelPreview.emit(reapplyRules);
  }
}
