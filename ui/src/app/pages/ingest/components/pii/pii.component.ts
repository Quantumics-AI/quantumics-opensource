import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PiiOutliersService } from '../../services/pii-outliers.service';

@Component({
  selector: 'app-pii',
  templateUrl: './pii.component.html',
  styleUrls: ['./pii.component.scss']
})
export class PiiComponent implements OnInit {
  @Input() projectId: number;
  @Input() folderId: number;
  @Input() fileId: number;
  loading: boolean;
  data = [];
  columns = [];
  Object = Object;

  constructor(
    private piiOutliersService: PiiOutliersService,
    public modal: NgbActiveModal) { }

  ngOnInit(): void {
    this.getPiiData();
  }

  getPiiData(): void {
    this.loading = true;
    this.data = [];

    this.piiOutliersService.getPIIData(this.projectId, this.folderId, this.fileId).subscribe((resposne) => {
      this.loading = false;
      if (resposne.status == 200) {
        this.data = JSON.parse(resposne?.data);
        const columnNames = Object.keys(this.data[0]);
        columnNames.forEach(col => {
          this.columns.push(col);
        });
      }
    }, (error) => {
      console.log(error);
      this.loading = false;
    });
  }

  close(): void {
    this.modal.close();
  }
}
