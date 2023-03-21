import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { JoinTypes, PreviewContent, SelectedJoin } from '../../models/configuration';
import { PreviewComponent } from '../preview/preview.component';

@Component({
  selector: 'app-preview-join',
  templateUrl: './preview-join.component.html',
  styleUrls: ['./preview-join.component.scss']
})
export class PreviewJoinComponent implements OnInit {

  @Input() selectedJoin: SelectedJoin;
  @Output() saveJoinData = new EventEmitter<any>();
  @Output() updateJoinImage = new EventEmitter<any>();
  @Output() closePreviewPanel = new EventEmitter<boolean>();

  public fg: FormGroup;
  public files: any;
  public joinTypes: JoinTypes;
  public previewHover: boolean;
  public activeTab: string;

  constructor(
    private fb: FormBuilder,
    private modalService: NgbModal) {

    this.joinTypes = new JoinTypes();

    this.fg = this.fb.group({
      joinType: new FormControl('', Validators.required),
      firstFileColumn: new FormControl('', Validators.required),
      secondFileColumn: new FormControl('', Validators.required)
    });
  }

  ngOnInit(): void { }

  ngOnChanges(changes: SimpleChanges): void {
    this.activeTab = 'lnkJoin';
    this.files = this.selectedJoin?.joinFolders;

    setTimeout(() => {
      this.fg.controls.joinType.setValue(this.selectedJoin?.joinType);
      this.fg.controls.firstFileColumn.setValue(this.selectedJoin?.firstFileColumn);
      this.fg.controls.secondFileColumn.setValue(this.selectedJoin?.secondFileColumn);
    }, 10);
  }

  save() {
    if (this.fg.invalid) {
      return;
    }

    this.saveJoinData.emit({
      eventId: this.selectedJoin?.eventId,
      joinType: this.fg.value.joinType,
      firstFileColumn: this.fg.value.firstFileColumn,
      secondFileColumn: this.fg.value.secondFileColumn
    });

    this.selectedJoin.content = new PreviewContent();
    this.selectedJoin.content.disabledPreview = true;
    this.selectedJoin.content.isDisabledApply = true;
  }

  public onSelectJoin(): void {
    this.updateJoinImage.emit({
      joinType: this.fg.value.joinType,
      eventId: this.selectedJoin?.eventId
    });
  }

  public viewFullPreviewData(): void {

    if (!this.selectedJoin?.content?.rowData?.length) {
      return;
    }

    const modalRef = this.modalService.open(PreviewComponent,
      { size: 'lg', backdrop: 'static' }
    );
    modalRef.componentInstance.data = this.selectedJoin.content.rowData;
    modalRef.componentInstance.columns = this.selectedJoin.content.rowColumns;
    modalRef.result.then((result) => { }, (error) => { });
  }

  public closePanel(): void {
    this.closePreviewPanel.emit(true);
  }
}
