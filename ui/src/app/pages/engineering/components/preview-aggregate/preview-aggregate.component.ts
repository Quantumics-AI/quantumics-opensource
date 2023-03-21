import { CdkDragDrop, copyArrayItem, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, EventEmitter, Input, OnInit, Output, Renderer2, SimpleChanges } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { PreviewContent, SelectedAggregate } from '../../models/configuration';
import { EngineeringService } from '../../services/engineering.service';
import { PreviewComponent } from '../preview/preview.component';

@Component({
  selector: 'app-preview-aggregate',
  templateUrl: './preview-aggregate.component.html',
  styleUrls: ['./preview-aggregate.component.scss']
})
export class PreviewAggregateComponent implements OnInit {

  @Input() aggregate: SelectedAggregate;
  @Output() previewData = new EventEmitter<void>();
  @Output() closePreviewPanel = new EventEmitter<boolean>();

  columns = [];
  data = [];
  groupByColumns = [];
  aggregateColumns = [];
  statusCompleted: boolean;
  previewHover: boolean;
  previousIndex: number;
  currentX: number;
  currentY: number;
  startX: any;
  startY: any;
  searchTerm: any = { value: '' };

  constructor(
    private _renderer: Renderer2,
    private engineeringService: EngineeringService,
    private snakbar: SnackbarService,
    private modalService: NgbModal) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.columns = this.aggregate.columns;
    this.groupByColumns = this.aggregate.groupByColumnsMetaData ?? [];
    this.aggregateColumns = this.aggregate.columnsMetaData ?? [];
  }

  removeAggregateColumn(item: any): void {
    this.aggregateColumns = this.aggregateColumns.filter(t => (t !== item) || (t.key !== item?.key));
    this.aggregate.columnsMetaData = this.aggregate.columnsMetaData.filter(t => (t !== item) || (t.key !== item?.key));

    if (item.columnName && item.aliasName) {
      this.columns.push({ key: item.columnName, value: item.aliasName });
    } else {
      this.columns.push({ key: item.key, value: item.value });
    }
  }

  removeGroupBycolumn(item: any): void {
    this.groupByColumns = this.groupByColumns.filter(t => (t !== item) || (t.key !== item?.key));
    this.aggregate.groupByColumnsMetaData = this.aggregate.groupByColumnsMetaData.filter(t => (t !== item) || (t.key !== item?.key));
    this.columns.push(item);
  }

  apply(): void {
    const groupByColumns = this.groupByColumns.map(g => g.key);

    if (!groupByColumns.length) {
      this.snakbar.open(`Please select the Grouped field`);
      return;
    }

    const columns = this.aggregateColumns.map(a => {
      return {
        opName: a.opName,
        columnName: a.key ?? a.columnName,
        aliasName: a.value ?? a.aliasName
      };
    });

    // Need to use model to avoid this code.
    const c1 = columns.find(x => !x.opName);
    if (c1) {
      this.snakbar.open(`Please select the aggregate field for ${c1.aliasName} column`);
      return;
    }

    const d = {
      projectId: this.aggregate.projectId,
      eventId: this.aggregate.eventId,
      dataFrameEventId: this.aggregate.dataFrameEventId,
      groupByColumns,
      columns
    };

    const aggregateColumns = this.aggregateColumns.map(t => {
      return {
        opName: t.opName,
        columnName: t.key ?? t.columnName,
        aliasName: t.value ?? t.aliasName,
      };
    });

    if (!aggregateColumns.length) {
      this.snakbar.open(`Please select the Aggregate field`);
      return;
    }

    this.engineeringService.createAggregateEvent(d).subscribe((response) => {
      this.aggregate.groupByColumns = groupByColumns;
      this.aggregate.columns = columns;
      this.aggregate.groupByColumnsMetaData = this.groupByColumns;
      this.aggregate.columnsMetaData = aggregateColumns;

      this.snakbar.open('Save Aggregation');
      this.previewData.emit();

      this.aggregate.content = new PreviewContent();
      this.aggregate.content.disabledPreview = true;
      this.aggregate.content.isDisabledApply = true;
    });
  }

  drop(event: CdkDragDrop<string[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    } else {
      const item: any = event.previousContainer.data[this.previousIndex];
      const includes = event.container.data?.find((t: any) => t.key === item.key);
      if (!includes) {
        copyArrayItem(event.previousContainer.data,
          event.container.data,
          this.previousIndex,
          event.currentIndex);

        // remove from columns
        const idx = this.columns.findIndex(x => x.key === item.key);
        this.columns.splice(idx, 1);
      }
    }

    this.aggregate.groupByColumnsMetaData = this.groupByColumns;
    this.aggregate.columnsMetaData = this.aggregateColumns;
  }

  dragTouchEndEvent(columns, column) {
    this.previousIndex = columns.findIndex(e => e === column);
  }

  dragMoved(e, action) {
    this.currentX = e.event.clientX;
    this.currentY = e.event.clientY;

    this._renderer.setStyle(e.source.element.nativeElement, 'border-style', 'none');
  }

  dragStart(e, action) {
    this._renderer.setStyle(e.source.element.nativeElement, 'border', '1px solid #D0D2D8');
    this._renderer.setStyle(e.source.element.nativeElement, 'border-color', '#D0D2D8');
    this._renderer.setStyle(e.source.element.nativeElement, 'background-color', '#fff');
  }

  public closePanel(): void {
    this.closePreviewPanel.emit(true);
  }

  public viewFullPreviewData(): void {

    if (!this.aggregate?.content?.rowData?.length) {
      return;
    }

    const modalRef = this.modalService.open(PreviewComponent,
      { size: 'lg', backdrop: 'static' }
    );
    modalRef.componentInstance.data = this.aggregate.content.rowData;
    modalRef.componentInstance.columns = this.aggregate.content.rowColumns;
    modalRef.result.then((result) => { }, (error) => { });
  }
}
