import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { FileDeltaService } from '../../services/file-delta.service';

@Component({
  selector: 'app-delta',
  templateUrl: './delta.component.html',
  styleUrls: ['./delta.component.scss']
})
export class DeltaComponent implements OnInit {
  @Input() projectId: number;
  @Input() folderId: number;
  @Input() selectedFileId: number;
  @Input() folderName: string;

  data: any = [];
  columns = [];
  files1 = [];
  files2 = [];
  loading: boolean;
  file2Id: number;
  query: any = '';

  sourceData$: Observable<any>;

  constructor(
    public modal: NgbActiveModal,
    private fileDeltaService: FileDeltaService) {
  }

  ngOnInit(): void {
    this.getFiles();
  }

  close(): void {
    this.modal.close();
  }

  getFiles() {
    this.loading = true;
    this.fileDeltaService.getFiles(this.projectId, this.folderId).subscribe((res: any) => {
      if (res?.code == 200) {
        this.files1 = res?.result;
        this.files2 = [...this.files1];
        const index = this.files1.findIndex(t => t.fileId == this.selectedFileId);
        this.files2.splice(index, 1);

        this.file2Id = this.files2.length ? this.files2[0]?.fileId : 0;
      }
      this.loading = false;
    }, () => {
      this.loading = false;
    });
  }

  getData(): void {

    this.columns = [];
    this.data = [];
    this.loading = true;

    this.fileDeltaService.getFileDelta(this.projectId, this.folderId, this.selectedFileId, this.file2Id).subscribe((res: any) => {
      const d = JSON.parse(res?.data);
      this.data = d?.difference;
      this.columns = Object.keys(this.data[0]);
      const index = this.columns.findIndex(t => t.toLowerCase() == 'column_name');
      this.columns.splice(index, 1);
      this.loading = false;
    });
  }
}
