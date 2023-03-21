import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SourceDataService } from 'src/app/pages/ingest/services/source-data.service';
import { decrypt } from 'src/app/core/utils/decryptor';

@Component({
  selector: 'app-view-dataset',
  templateUrl: './view-dataset.component.html',
  styleUrls: ['./view-dataset.component.scss']
})
export class ViewDatasetComponent implements OnInit {
  @Input() projectId: number;
  @Input() userId: number;
  @Input() folderId: number;
  @Input() fileId: number;

  loading = false;
  newColumns = [];
  columns: any;
  data: any;
  rowLength: any;
  columnLength: any;

  private piiColumns: Array<string>;

  constructor(
    public modal: NgbActiveModal,
    private sourceDataService: SourceDataService,
  ) { }

  ngOnInit(): void {
    this.getFileData();
  }

  public getFileData(): void {
    this.loading = true;
    const params = {
      userId: this.userId,
      projectId: this.projectId,
      folderId: this.folderId,
      fileId: this.fileId,
    };

    const datatypes = [];
    this.newColumns = [];

    this.sourceDataService.getFileContent(params).subscribe((response) => {
      this.data = JSON.parse(decrypt(response.data));
      this.columns = JSON.parse(decrypt(response.metadata));
      let temp;

      try {
        temp = JSON.parse(response.file_additional_info)?.encryptPiiColumns?.split(',');
      } catch (error) {
        
      }
      this.piiColumns = temp ? temp : [];

      this.newColumns = this.columns.map(c => {
        datatypes.push(c.data_type);

        return {
          column_name: c.column_name,
          display_name: c.column_name,
          data_type: c.data_type,
          barChartLabel: [],
          barChartData: []
        };
      });
      const uniques = [...new Set(datatypes)];
      this.columnLength = this.columns.length;
      this.rowLength = this.data.length;
      this.loading = false;
    }, () => {
      this.loading = false;
    });
  }

  public isPIIColumn(columnName: string): boolean {
    return this.piiColumns.includes(columnName);
  }

}
