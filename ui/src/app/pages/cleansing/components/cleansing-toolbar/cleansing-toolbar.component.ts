import { Component, Output, EventEmitter, Input, ViewEncapsulation, OnChanges, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ExtractRuleTypes } from '../../constants/extract-rule-types';
import { FilterRuleTypes } from '../../constants/filter-rule-types';
import { FormatRuleTypes } from '../../constants/format-rule-types';
import { ManageColumnRuleTypes } from '../../constants/manage-column-rule-types';
import { SplitRuleTypes } from '../../constants/split-rule-types';
import { CountMatchRuleTypes } from '../../constants/count-match-rule-types';
import { AutomationService } from 'src/app/pages/automation/services/automation.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { MergeRuleTypes } from '../../constants/merge-rule-types';

@Component({
  selector: 'app-cleansing-toolbar',
  templateUrl: './cleansing-toolbar.component.html',
  styleUrls: ['./cleansing-toolbar.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class CleansingToolbarComponent implements OnChanges {
  @Output() editor = new EventEmitter<any>();
  @Input() appliedRuleCount: number;
  @Input() resetMenuTopbarColor: boolean;

  private projectId: number;
  private fileId: number;
  public formatRuleTypes = FormatRuleTypes;
  public filterRuleTypes = FilterRuleTypes;
  public splitRuleTypes = SplitRuleTypes;
  public manageColumnRuleTypes = ManageColumnRuleTypes;
  public extractRuleTypes = ExtractRuleTypes;
  public countMatchRuleTypes = CountMatchRuleTypes
  public mergeRuleTypes = MergeRuleTypes;
  public activeMenuIndex: number;
  public hasJobRunning: boolean;

  public inActiveImages: string[] = [
    'assets/media/preparation/filterRows-inactive.svg',
    'assets/media/preparation/replace-inactive.svg',
    'assets/media/preparation/extract-inactive.svg',
    'assets/media/preparation/split-inactive.svg',
    'assets/media/preparation/merge-inactive.svg',
    'assets/media/preparation/format-inactive.svg',
    'assets/media/preparation/standardise-inactive.svg',
    'assets/media/preparation/manageColumns-inactive.svg',
    'assets/media/preparation/countMatches-inactive.svg',
  ];

  public activeImages: string[] = [
    'assets/media/preparation/filterRows-active.svg',
    'assets/media/preparation/replace-active.svg',
    'assets/media/preparation/extract-active.svg',
    'assets/media/preparation/split-active.svg',
    'assets/media/preparation/merge-active.svg',
    'assets/media/preparation/format-active.svg',
    'assets/media/preparation/standardise-active.svg',
    'assets/media/preparation/manageColumns-active.svg',
    'assets/media/preparation/countMatches-active.svg',
  ];

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private snakbar: SnackbarService,
    private automationService: AutomationService) {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
    this.fileId = +this.activatedRoute.snapshot.paramMap.get('fileId');
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.activeMenuIndex = -1;
  }

  setEditorComponent(editorComponent, ruleInputLogic1: string = '') {
    this.editor.emit({ editorOpen: true, editorComponent, ruleInputLogic1 });
  }

  public redirectToFolder(): void {
    this.router.navigate([`projects/${this.projectId}/cleansing`]);
  }


  public runJob(): void {
    this.hasJobRunning = true;
    const params = {
      projectId: this.projectId,
      fileId: this.fileId
    };

    this.automationService.runJob(params).subscribe((res) => {
      this.hasJobRunning = false;
      if (res.code === 200) {
        this.snakbar.open('Job run initiated successfully.');
      } else {
        this.snakbar.open(res?.message);
      }
    }, (error) => {
      this.hasJobRunning = false;
      this.snakbar.open(error);
    });
  }
}

