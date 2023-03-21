import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { RuleTypes } from '../../constants/rule-types';
import { CleansingDataService } from '../../services/cleansing-data.service';

@Component({
  selector: 'app-standardize',
  templateUrl: './standardize.component.html',
  styleUrls: ['./standardize.component.scss']
})
export class StandardizeComponent implements OnInit, OnDestroy {
  public cleansingParamId: number;
  public ruleSequence: number;
  public isUpdate: boolean;
  public projectId: number;
  public folderId: number;
  public fileId: number;
  public rows: Array<any>;
  public columns: Array<any>;
  public groupedRows = [];
  public newValue: string;
  public ruleInputValues1: string;
  public selectedColumn: string;
  public selectedAll: boolean;
  public sortBy = 'count';
  public sortDirection: 'asc' | 'desc' = 'asc';
  public updateView = false;
  private unsubscribe: Subject<void> = new Subject();
  public selectColumn = false;
  public selectNewValue = false;

  constructor(public modal: NgbActiveModal, private cleansingDataService: CleansingDataService) { }

  ngOnInit(): void {
    this.handleColumnSelection(this.selectedColumn);
  }

  public handleColumnSelection(columnName: string): void {
    
    // this.selectColumn = columnName;
    // console.log(this.selectColumn);
    this.groupedRows.length = 0;
    if (!columnName) {
      this.groupedRows = [];
      return;
    }

    // if(columnName == "undefined"){
    //   this.selectColumn = false;
      
    // }else {
    //   this.selectColumn = true;
    // }

    this.cleansingDataService
      .getCountsForStandardizeRule(this.projectId, this.folderId, this.fileId, columnName)
      .pipe(takeUntil(this.unsubscribe),
        map(res => res?.code === 200 ? res.result : [[]]),
        map(res => res[0])
      ).subscribe(res => {
        this.groupedRows = res;
        if (this.ruleInputValues1) {
          const colValues = this.ruleInputValues1.split('|');
          const selectedValues = this.groupedRows.filter(r => colValues.indexOf(r.column_value) >= 0);
          selectedValues.forEach(element => {
            element.selected = true;
          });
        }
      });
  }

  public selectAll(selected: boolean): void {
    if(selected == true){
      this.groupedRows.forEach(r => r.selected = selected);
      this.selectColumn = true;

    } else {
      this.groupedRows.forEach(r => r.selected = selected);
      this.selectColumn = false;
      
    }

    
  }

  public handleSelection(): void {
    this.selectedAll = this.groupedRows.length === this.groupedRows.filter(r => r.selected).length;
    if(this.groupedRows.find(r => r.selected) != undefined){
      this.selectColumn = true;
      
    } else {
      this.selectColumn = false;
    }
    
  }

  modelChangedNewValue(obj) {
    if (obj != "") {
      this.selectNewValue = true;
      
    } else {
      this.selectNewValue = false;
    }
    
  }

  public apply(): void {
    const rule = {
      ruleInputLogic: RuleTypes.Standardize,
      ruleOutputValues: '',
      ruleImpactedCols: [this.selectedColumn],
      ruleInputValues: this.newValue,
      ruleInputValues1: this.groupedRows.filter(r => r.selected).map(r => r.column_value).join('|'),
      update: this.isUpdate,
      cleansingParamId: this.isUpdate ? this.cleansingParamId : 0,
      ruleSequence: this.isUpdate ? this.ruleSequence : 0,
    };

    this.modal.close(rule);
  }

  public sort(sortBy: string): void {
    if (sortBy === this.sortBy) {
      if (this.sortDirection === 'asc') {
        this.sortDirection = 'desc';
      } else {
        this.sortDirection = 'asc';
      }
    } else {
      this.sortDirection = 'asc';
    }

    this.sortBy = sortBy;
    this.updateView = !this.updateView;
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}