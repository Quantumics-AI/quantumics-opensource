import { Component, Output, EventEmitter, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ExtractRuleTypes } from '../../constants/extract-rule-types';
import { FilterRuleTypes } from '../../constants/filter-rule-types';
import { FormatRuleTypes } from '../../constants/format-rule-types';
import { ManageColumnRuleTypes } from '../../constants/manage-column-rule-types';
import { SplitRuleTypes } from '../../constants/split-rule-types';

@Component({
  selector: 'app-cleansing-toolbar',
  templateUrl: './cleansing-toolbar.component.html',
  styleUrls: ['./cleansing-toolbar.component.scss']
})
export class CleansingToolbarComponent {
  @Output() editor = new EventEmitter<any>();
  @Input() hasRulesCatalogue: boolean;

  private projectId: number;

  public formatRuleTypes = FormatRuleTypes;
  public filterRuleTypes = FilterRuleTypes;
  public splitRuleTypes = SplitRuleTypes;
  public manageColumnRuleTypes = ManageColumnRuleTypes;
  public extractRuleTypes = ExtractRuleTypes;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute) {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
  }

  setEditorComponent(editorComponent, ruleInputLogic1: string = '') {
    this.editor.emit({ editorOpen: true, editorComponent, ruleInputLogic1 });
  }

  public redirectToFolder(): void {
    this.router.navigate([`projects/${this.projectId}/cleansing`]);
  }
}

