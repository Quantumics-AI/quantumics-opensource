import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Column } from '../../models/file-stats';

@Component({
  selector: 'app-columns',
  templateUrl: './columns.component.html',
  styleUrls: ['./columns.component.scss']
})
export class ColumnsComponent implements OnChanges {
  @Input() columns: Column[];
  @Output() selectedColumn = new EventEmitter<Column>();
  @Input() analysis: any;

  public searchTerm: string;
  public selectedColumnValue: string;

  public isDescending: boolean;

  ngOnChanges(changes: SimpleChanges): void {
    if (this.columns.length) {
      const selectedColumn = this.columns.find(x=> x.selected);
      this.selectedColumnValue = selectedColumn?.column;
    }
  }

  public selectColumn(item: Column): void {
    this.selectedColumnValue = item.column;
    this.selectedColumn.emit(item);
  }

  public sortDataTypes(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.columns = this.columns.sort((a, b) => {
        var folder_name_order = a.dataType.localeCompare(b.dataType);
        return folder_name_order;
      });
    } else {
      this.columns = this.columns.sort((a, b) => {
        var folder_name_order = b.dataType.localeCompare(a.dataType);
        return folder_name_order;
      });
    }

  }
}

