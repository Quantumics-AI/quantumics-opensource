import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { SelectedFile } from '../../models/configuration';
import { PreviewComponent } from '../preview/preview.component';

@Component({
  selector: 'app-preview-file',
  templateUrl: './preview-file.component.html',
  styleUrls: ['./preview-file.component.scss']
})
export class PreviewFileComponent implements OnInit {
  @Input() selectedFile: SelectedFile;
  @Output() closePreviewPanel = new EventEmitter<boolean>();

  previewHover: boolean;

  constructor(
    private modalService: NgbModal) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
  }

  public viewFullPreviewData(): void {

    if (!this.selectedFile?.content?.rowData?.length) {
      return;
    }

    const modalRef = this.modalService.open(PreviewComponent,
      { size: 'lg', backdrop: 'static' }
    );
    modalRef.componentInstance.data = this.selectedFile.content.rowData;
    modalRef.componentInstance.columns = this.selectedFile.content.rowColumns;
  }

  public closePanel(): void {
    this.closePreviewPanel.emit(true);
  }

  ngOnDestroy(): void {
  }
}
