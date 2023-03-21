import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { StpperType } from '../../enums/stepper';
import { Pii, PiiData, PiiDataResult } from '../../models/pii';
import { Database } from '../../models/database';
import { Folder } from '../../models/folder';
import { ActivatedRoute, Router } from '@angular/router';
import { DataService } from '../../services/data-service';

@Component({
  selector: 'app-pii-identification',
  templateUrl: './pii-identification.component.html',
  styleUrls: ['./pii-identification.component.scss']
})
export class PiiIdentificationComponent implements OnInit {

  public pii: Array<Pii>;
  public tables: Array<Database> = [];
  public searchByTable: string;
  public sourceType: string;
  public isVisiablePii: boolean;

  private piiDataResult: Array<PiiDataResult> = [];
  private selectedTable: string;
  private projectId: number;
  private piiData: Array<PiiData>;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private dataservice: DataService) {
    this.sourceType = this.activatedRoute.snapshot.paramMap.get('sourceType');
    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
  }

  ngOnInit(): void {
    this.dataservice.updateProgress(60);
    this.piiData = JSON.parse(sessionStorage.getItem('rowPii')) as Array<PiiData>;

    switch (this.sourceType) {
      case 'pgsql':
        this.piiData.map(t => {
          const tbl = {
            name: t.TABLE_NAME,
            isClick: false
          } as Database;
          this.tables.push(tbl);
        });

        this.selectionTable(null, this.piiData[0]?.TABLE_NAME);
        break;
      case 'txt':
        this.selectionTable(null, this.piiData[0]?.TABLE_NAME);
        break;
      default:
        this.selectionTable(null, this.piiData[0]?.TABLE_NAME);
        break;
    }
  }

  public updatePiiStatus(event: any): void {  
    this.pii.find(x => x.column === event.target.value).isPII = event.target.checked;
    if(this.pii.find(x=> x.column === event.target.value).isPII){
      this.pii.find(x => x.column === event.target.value).piiMaskActionType = "Don't Ingest";
    } else {
      this.pii.find(x => x.column === event.target.value).piiMaskActionType = '';
    }
    

    const piiResult = this.piiDataResult.find(x => x.TableName === this.selectedTable);
    piiResult.pii = this.pii;
  }

  public upatePiiMaskingStatus(event: any, column: string): void {
    const d = this.pii.find(x => x.column === column);
    d.piiMaskActionType = event.value;

    const piiResult = this.piiDataResult.find(x => x.TableName === this.selectedTable);
    piiResult.pii = this.pii;
  }

  public continue(): void {

    if (this.piiDataResult.length !== this.piiData.length) {
      // add the remain data into object
      this.piiData.map(t => {
        this.selectionTable(null, t.TABLE_NAME);
      });
    }
    sessionStorage.setItem('piiResult', JSON.stringify(this.piiDataResult));
    this.router.navigate([`projects/${this.projectId}/ingest/completion/${this.sourceType}`]);

  }

  public selectionTable(event: any, tableName: string): void {
    this.selectedTable = tableName;
    this.tables.map(t => t.isClick = t.name === tableName ? true : false);

    for (const item of this.piiData) {
      if (item.TABLE_NAME === tableName) {
        if (+item.code !== 200) {
          this.pii = [];
          break;
        }

        const tbl = this.piiDataResult.find(x => x.TableName === tableName);
        if (tbl) {
          this.pii = tbl.pii;
        } else {
          const result = JSON.parse(item?.PII_COLUMNS_INFO) as any;
          this.pii = [];

          const p: Array<Pii> = [];

          result.map(t => {
            const d = new Pii();
            d.column = t.Column;
            d.isPII = t.PII?.toLowerCase() === 'yes' ? true : false;
            d.piiMaskActionType = d.isPII ? 'Don\'t Ingest' : '';
            p.push(d);
          });

          const folder = new Folder();
          folder.folderId = +item.Folder_Id;
          folder.file.filePath = item.CSV_FILE_PATH;
          folder.file.fileName = item.CSV_FILE_PATH ? item.CSV_FILE_PATH.split('/')?.pop() : "";
          folder.folderName = item.TABLE_NAME;

          this.piiDataResult.push({
            folder,
            pii: p,
            CSV_FILE_PATH: item.CSV_FILE_PATH,
            TableName: item.TABLE_NAME
          });

          this.pii = p;

          break;
        }
      }
    }
  }
}
