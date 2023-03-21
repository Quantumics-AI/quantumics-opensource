import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PiiOutliersService } from '../../services/pii-outliers.service';

@Component({
  selector: 'app-outliers',
  templateUrl: './outliers.component.html',
  styleUrls: ['./outliers.component.scss']
})
export class OutliersComponent implements OnInit {
  @Input() projectId: number;
  @Input() folderId: number;
  @Input() fileId: number;
  loading: boolean;
  data = [];
  columns = [];

  constructor(
    private piiOutliersService: PiiOutliersService,
    public modal: NgbActiveModal) { }

  ngOnInit(): void {
    this.getOutliersData();
  }

  getOutliersData(): void {
    this.loading = true;
    this.data = [];

    this.piiOutliersService.getOutliersData(this.projectId, this.folderId, this.fileId).subscribe((resposne) => {
      this.loading = false;
      if (resposne.status == 200) {
        const res = JSON.parse(resposne?.data);
        this.columns = res?.columns;
        this.data = res?.data;

        // Insert first default column RowNo
        this.columns.unshift('Row Number');

        // Inser index to first position of data.
        this.data.map((t, index) => {
          t.unshift(res.index[index]);
        });
      }
    }, () => {
      this.loading = false;
    });
  }

  close(): void {
    this.modal.close();
  }
}
