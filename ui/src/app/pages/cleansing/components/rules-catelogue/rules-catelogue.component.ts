import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges, OnChanges } from '@angular/core';

import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { ActivatedRoute } from '@angular/router';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { CleansingDataService } from '../../services/cleansing-data.service';
import { ExtractRuleTypes } from '../../constants/extract-rule-types';
import { Rule } from '../../models/rule';
import { FormatRuleTypes } from '../../constants/format-rule-types';
import { ManageColumnRuleTypes } from '../../constants/manage-column-rule-types';
import { RuleTypes } from '../../constants/rule-types';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DeleteComfirmationComponent } from '../delete-comfirmation/delete-comfirmation.component';
import { ReplaceMissingRuleTypes } from '../../constants/replace-missing-rule-types';
import { SplitRuleTypes } from '../../constants/split-rule-types';

@Component({
  selector: 'app-rules-catelogue',
  templateUrl: './rules-catelogue.component.html',
  styleUrls: ['./rules-catelogue.component.scss']
})

export class RulesCatelogueComponent implements OnInit, OnChanges {
  @Output() deleted = new EventEmitter<any>();
  @Output() edit = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<boolean>();
  @Output() sequenceChanged = new EventEmitter<void>();
  @Input() rules: any[];
  public redraw = true;
  selectedRule: any;
  showRulesCatelogue = true;
  private projectId: number;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  rulesStatement = [];
  dragged = false;
  updateView = false;
  changeElemParam: number;
  updatedSequenceList = [];
  private unsubscribe: Subject<void> = new Subject();

  constructor(
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private cleansingDataService: CleansingDataService,
    private snackBar: SnackbarService,
    private modalService: NgbModal) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$.pipe(takeUntil(this.unsubscribe)).subscribe(certificate => {
      this.certificateData = certificate;
    });
  }

  ngOnInit(): void {
    this.rules.forEach(r => r.statement = this.constructStatement(r));
    this.setDependancyFlags();
    this.dragged = false;
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
    this.rules.length > 0 ? this.showRulesCatelogue = true : this.showRulesCatelogue = false;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.rules && !changes.rules.firstChange) {
      this.rules.forEach(r => r.statement = this.constructStatement(r));
      this.setDependancyFlags();
    }
  }

  goToRules(item: any) {
    this.edit.emit(item);
  }

  drop(event: CdkDragDrop<string[]>) {
    const isValid = this.validateDrop(event.previousIndex, event.currentIndex);
    if (!isValid) {
      this.snackBar.open('This drop sequence is not valld');
      return;
    }

    moveItemInArray(this.rules, event.previousIndex, event.currentIndex);
    const newSequence = event.currentIndex + 1;
    const oldSequence = event.previousIndex + 1;

    this.rules.forEach(element => {
      if (element.ruleSequence === newSequence) {
        this.changeElemParam = element.cleansingParamId;
      }
    });

    this.rules.forEach(element => {
      if (oldSequence === element.ruleSequence) {
        element.ruleSequence = newSequence;
        const changedItem = { cleansingParamId: element.cleansingParamId, ruleSequence: 0 };
        changedItem.ruleSequence = newSequence;
        this.updatedSequenceList.push(changedItem);
      }
      if (element.cleansingParamId === this.changeElemParam) {
        element.ruleSequence = oldSequence;
        const changedItem = { cleansingParamId: element.cleansingParamId, ruleSequence: 0 };
        changedItem.ruleSequence = oldSequence;
        this.updatedSequenceList.push(changedItem);
      }
    });

    this.dragged = true;
    this.updateView = !this.updateView;
  }

  public updateSequence(): void {
    this.cleansingDataService.updateRuleSequence(this.updatedSequenceList, this.projectId).subscribe((response: any) => {
      if (response.code === 200) {
        this.snackBar.open(response.message);
        this.sequenceChanged.emit();
        this.dragged = false;
      } else {
        this.snackBar.open(response.message);
      }
    });
  }

  private validateDrop(previousSequence: number, currentSequence: number): boolean {
    const dragedRule = this.rules[previousSequence];
    let dependentRules = this.getDependentRules(dragedRule.cleansingParamId);
    let isParent = dependentRules.length > 0;
    let isValid = true;

    if (isParent) {
      isValid = this.validateParentDrop(currentSequence, dependentRules);
    } else {
      const parentRuleIds = dragedRule.parentRuleIds?.split(',').map(p => +p) ?? [];
      isValid = this.validateChildDrop(currentSequence, parentRuleIds);
    }

    // decision made
    if (!isValid) {
      return isValid;
    }

    const droppedIntoRule = this.rules[currentSequence];
    dependentRules = this.getDependentRules(droppedIntoRule.cleansingParamId);
    isParent = dependentRules.length > 0;

    if (isParent) {
      isValid = this.validateParentDrop(previousSequence, dependentRules);
    } else {
      const parentRuleIds = dragedRule.parentRuleIds?.split(',').map(p => +p) ?? [];
      isValid = this.validateChildDrop(previousSequence, parentRuleIds);
    }

    return isValid;
  }

  private validateParentDrop(currentSequence: number, dependentRules: any): boolean {
    let isValid = true;
    for (const rule of dependentRules) {
      if ((currentSequence + 1) >= rule.ruleSequence) {
        isValid = false;
        break;
      }
    }

    return isValid;
  }

  private validateChildDrop(currentSequence: number, parentRuleIds: Array<number>): boolean {
    let isValid = true;
    for (const ruleId of parentRuleIds) {
      const parentRuleSequence = this.rules.find(r => r.cleansingParamId === ruleId)?.ruleSequence;
      if (parentRuleSequence >= (currentSequence + 1)) {
        isValid = false;
        break;
      }
    }

    return isValid;
  }

  private getDependentRules(parentRuleId: number): any {
    return this.rules.filter(c => c.parentRuleIds?.split(',')
      .map(p => +p)
      .includes(parentRuleId)
    );
  }

  public deleteRule(delItem): void {
    const dependentRules = this.getDependentRules(delItem.cleansingParamId);
    for (const rule of dependentRules) {
      rule.flag = true;
      rule.errorMsg = 'One of selected columns is missing';
    }

    if (dependentRules.length > 0) {
      this.modalService.open(DeleteComfirmationComponent).result.then((result) => {
        this.delete(delItem.cleansingParamId);
      }, (reason) => {
        for (const rule of dependentRules) {
          rule.flag = false;
          rule.errorMsg = '';
        }
      });
    } else {
      this.delete(delItem.cleansingParamId);
    }
  }

  public close(): void {
    this.cancel.emit(false);
  }

  private delete(ruleId: number): void {
    const userId = this.certificateData.user_id;
    const deleteRule = { projectId: this.projectId, ruleId, userId };
    this.cleansingDataService.deleteRules(deleteRule).subscribe((response: any) => {
      if (response?.code === 200) {
        this.snackBar.open(response.message);
        this.deleted.emit(ruleId);
      } else {
        this.snackBar.open(response.message);
      }
    });
  }

  private setDependancyFlags(): void {
    const ruleIds = this.rules.map(r => r.cleansingParamId);
    for (const rule of this.rules) {
      const parentRuleIds = rule.parentRuleIds?.split(',').map(p => +p).filter(p => p > 0) ?? [];
      const missingParentRules = parentRuleIds.filter(e => !ruleIds.includes(e));
      if (missingParentRules.length > 0) {
        rule.flag = true;
        rule.errorMsg = 'One of selected columns is missing';
      }
    }
  }

  private constructStatement(rule: any): string {
    let stmnt = '';

    switch (rule.ruleInputLogic) {
      case RuleTypes.Format:
        stmnt = this.formatRuleStatement(rule);
        break;
      case 'findReplace':
        const replaceWithStr = rule.ruleInputValues1 ? rule.ruleInputValues1 : null;
        stmnt = `Replace matches of ${rule.ruleInputValues} from ${rule.ruleImpactedCols} with '${replaceWithStr}'`;
        break;
      case RuleTypes.PatternReplace:
        stmnt = `Replace cells which equals ${rule.ruleInputValues} from ${rule.ruleImpactedCols} with '${rule.ruleInputValues1}'`;
        break;
      case 'fillNull':
        const replaceWith = this.getReplaceWith(rule);
        stmnt = `Replace missing values of ${rule.ruleImpactedCols} with ${replaceWith}`;
        break;
      case RuleTypes.SplitColumnValue:
        stmnt = this.splitRuleStatement(rule);
        break;
      case RuleTypes.CountMatch:
        if (rule.ruleInputLogic1 === 'Delimiter') {
          stmnt = `Count occurrences starting from ${rule.ruleInputValues} ending at ${rule.ruleInputValues1} in ${rule.ruleImpactedCols}`;
        } else {
          stmnt = `Count occurrences of ${rule.ruleInputValues} in ${rule.ruleImpactedCols}`;
        }
        break;
      case RuleTypes.Merge:
        stmnt = `Merge columns ${rule.ruleImpactedCols} to ${rule.ruleInputValues} column`;
        break;
      case RuleTypes.FilterRows:
      case RuleTypes.FilterRowsByColumn:
        stmnt = `Filter rows`;
        break;
      case RuleTypes.ManageColumns:
        stmnt = this.manageColumnRuleStatement(rule);
        break;
      case RuleTypes.ExtractColumnValues:
        if (rule.ruleInputLogic1 === ExtractRuleTypes.FirstCharacters) {
          stmnt = `Extract the first ${rule.ruleInputValues} characters from column ${rule.ruleImpactedCols}`;
        } else if (rule.ruleInputLogic1 === ExtractRuleTypes.LastCharacters) {
          stmnt = `Extract the last ${rule.ruleInputValues} characters from column ${rule.ruleImpactedCols}`;
        } else if (rule.ruleInputLogic1 === ExtractRuleTypes.Numbers) {
          stmnt = `Extract numbers from ${rule.ruleImpactedCols} ${rule.ruleInputValues} times`;
        } else if (rule.ruleInputLogic1 === ExtractRuleTypes.CharactersBetweenPostions) {
          stmnt = `Extract characters between ${rule.ruleInputValues} to ${rule.ruleInputValues1} from ${rule.ruleImpactedCols}`;
        } else if (rule.ruleInputLogic1 === ExtractRuleTypes.QueryStrings) {
          stmnt = `Extract Query Strings ${rule.ruleInputValues} from ${rule.ruleImpactedCols}`;
        } else if (rule.ruleInputLogic1 === ExtractRuleTypes.BetweenDelimiters) {
          stmnt = `Extract values using delimiter from ${rule.ruleImpactedCols}`;
        } else if (rule.ruleInputLogic1 === ExtractRuleTypes.TextOrPattern) {
          stmnt = `Extract values using Text or Pattern from ${rule.ruleImpactedCols}`;
        } else if (rule.ruleInputLogic1 === ExtractRuleTypes.TypeMismatched) {
          stmnt = `Extract Mismatched values from  ${rule.ruleImpactedCols} where match Type ${rule.ruleInputValues}`;
        }
        break;
      case RuleTypes.RemoveDuplicateRows:
        stmnt = 'Remove duplicate rows';
        break;
      case 'standardize':
        stmnt = `Standardize values for column ${rule.ruleImpactedCols}`;
        break;
      default:
        stmnt = '';
        break;
    }

    return stmnt;
  }

  private getReplaceWith(rule: any): string {
    let val = '';
    switch (rule.ruleInputLogic1) {
      case ReplaceMissingRuleTypes.Average:
        val = 'Average';
        break;
      case ReplaceMissingRuleTypes.CustomValue:
        val = rule.ruleInputValues3;
        break;
      case ReplaceMissingRuleTypes.LastValidValue:
        val = 'Last valid value';
        break;
      case ReplaceMissingRuleTypes.Mod:
        val = 'Mode';
        break;
      case ReplaceMissingRuleTypes.Sum:
        val = 'Sum';
        break;
      case ReplaceMissingRuleTypes.Null:
        val = 'Null';
        break;
      default:
        break;
    }
    return val;
  }

  private formatRuleStatement(rule: Rule): string {
    switch (rule.ruleInputLogic1) {
      case FormatRuleTypes.Lower:
      case FormatRuleTypes.Upper:
      case FormatRuleTypes.Proper:
        return `Format ${rule.ruleImpactedCols} to ${rule.ruleInputLogic1} case`;

      case FormatRuleTypes.TrimWhitespace:
        return `Trim whitespace from ${rule.ruleImpactedCols}`;

      case FormatRuleTypes.TrimQuotes:
        return `Trim quotes from ${rule.ruleImpactedCols}`;

      case FormatRuleTypes.RemoveWhitespaces:
        return `Remove whitespace from ${rule.ruleImpactedCols}`;

      case FormatRuleTypes.RemoveSpecialCharacters:
        return `Remove special characters from ${rule.ruleImpactedCols}`;

      case FormatRuleTypes.RemoveAccents:
        return `Remove accents from ${rule.ruleImpactedCols}`;

      case FormatRuleTypes.AddPrefix:
        return `Add ${rule.ruleInputValues} to the start of ${rule.ruleImpactedCols}`;

      case FormatRuleTypes.AddSuffix:
        return `Add ${rule.ruleInputValues} to the end of ${rule.ruleImpactedCols}`;

      case FormatRuleTypes.PadWithLeadingCharacters:
        return `Padd leading characters to ${rule.ruleImpactedCols}`;
    }
  }

  private manageColumnRuleStatement(rule: Rule): string {
    switch (rule.ruleInputLogic1) {
      case ManageColumnRuleTypes.Create:
        return `Create new column ${rule.ruleInputValues} after column ${rule.ruleImpactedCols}`;
      case ManageColumnRuleTypes.Drop:
        return `Drop column ${rule.ruleImpactedCols}`;
      case ManageColumnRuleTypes.Clone:
        return `Clone column ${rule.ruleImpactedCols}`;
      case ManageColumnRuleTypes.Rename:
        return `Rename column ${rule.ruleImpactedCols} to ${rule.ruleInputValues}`;
      case ManageColumnRuleTypes.DataTypeCast:
        return `${rule.ruleImpactedCols} data type changed from “old datatype” to ${rule.ruleInputValues}`;
    }
  }

  private splitRuleStatement(rule: Rule): string {
    switch (rule.ruleInputLogic1) {
      case SplitRuleTypes.ByDelimiter:
        return `Split ${rule.ruleImpactedCols} on Delimiter matching ${rule.ruleInputValues} into 2 columns`;
      case SplitRuleTypes.BetweenDelimiter:
        return `Split ${rule.ruleImpactedCols} on between ${rule.ruleInputValues} and ${rule.ruleInputValues1} delimiters into 2 columns`;
      case SplitRuleTypes.ByPositions:
        return `Split ${rule.ruleImpactedCols} by ${rule.ruleInputValues} positions`;
      case SplitRuleTypes.BetweenTwoPositions:
        return `Split ${rule.ruleImpactedCols} between positions ${rule.ruleInputValues} and ${rule.ruleInputValues1}`;
    }
  }
}
