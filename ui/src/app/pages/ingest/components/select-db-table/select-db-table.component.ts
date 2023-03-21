import { Component, OnInit } from '@angular/core';
import { DbConnectorService } from '../../services/db-connector.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Database, DatabasePayload, TableSelection } from '../../models/database';
import { Router } from '@angular/router';
import { DataService } from '../../services/data-service';

@Component({
  selector: 'app-select-db-table',
  templateUrl: './select-db-table.component.html',
  styleUrls: ['./select-db-table.component.scss']
})
export class SelectDbTableComponent implements OnInit {
  public userSelectedTable: string;
  public selectedTables: Array<TableSelection> = [];
  public schemas: Database[];
  public tables: Database[];

  public previewColumn = [];
  public previewRow = [];
  public isPreviewClicked: boolean;

  public loading: boolean;
  public queryType: string = 'default';
  public sqlType: string = 'D';
  public searchByQuery: string;
  public fg: FormGroup;

  private unsubscribe: Subject<void> = new Subject<void>();
  private connectionParams: DatabasePayload;

  constructor(
    private connectorService: DbConnectorService,
    private snackbar: SnackbarService,
    private fb: FormBuilder,
    private router: Router,
    private dataservice: DataService) {
  }

  ngOnInit(): void {
    this.dataservice.updateProgress(40);
    this.connectionParams = JSON.parse(sessionStorage.getItem('connectionParams')) as DatabasePayload;

    this.fg = this.fb.group({
      sqlQueryName: [{ value: '', disabled: true }, Validators.required]
    });

    this.getDatabaseSchemaList();
  }

  public previewData(): void {
    this.loading = true;
    this.previewColumn = [];
    this.previewRow = [];

    if (this.queryType === 'custom') {
      this.connectionParams.sql = this.fg.controls.sqlQueryName.value;
    } else {
      const selectedData = this.selectedTables.find(x => x.tableName === this.userSelectedTable);
      this.connectionParams.tableName = selectedData.tableName ?? this.connectionParams.tableName;
      this.connectionParams.sql = `select * from ${selectedData.schemaName}.${selectedData.tableName}`;
    }

    this.connectorService.preview(this.connectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((response) => {
        this.isPreviewClicked = true;
        this.loading = false;

        if (response && response.result?.length) {
          this.previewColumn = Object.keys(response.result[0])
          this.previewRow = response.result;
        }

      }, (error) => {
        this.isPreviewClicked = true;
        this.loading = false;
        this.snackbar.open(error);
      });
  }

  public downloadData(): void {
    if (this.queryType === 'custom') {
      this.connectionParams.tableName = `${this.connectionParams.pipelineName}_cs`;
      this.connectionParams.sql = this.fg.controls.sqlQueryName.value;
      this.connectionParams.schemaName = "";
    } else {
      this.connectionParams.sql = this.selectedTables.map(t => `select * from ${t.schemaName}.${t.tableName}`).join(';');
      const schemaNames = this.selectedTables.map(t => t.schemaName).join(';');
      this.connectionParams.schemaName = schemaNames;
      const tableNames = this.selectedTables.map(t => t.tableName).join(';');
      this.connectionParams.tableName = tableNames;
    }

    this.connectionParams.sqlType = this.sqlType;

    this.loading = true;

    const identifyPIIObj = {
      connectorType: this.connectionParams.connectorType,
      projectId: this.connectionParams.projectId,
      userId: this.connectionParams.userId,
      schemaName: this.connectionParams.schemaName,
      tableName: this.connectionParams.tableName,
      connectorId: this.connectionParams.connectorId,
      pipelineId: this.connectionParams.pipelineId,
      sql: this.connectionParams.sql,
      sqlType: this.sqlType,
    }

    this.connectorService
      .piiIdentify(identifyPIIObj)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(res => {
        this.loading = false;
        if (res?.result !== null && res?.result?.length > 0) {
          this.connectionParams.pipelineId = res?.pipelineId;
          sessionStorage.setItem('rowPii', JSON.stringify(res?.result));
          this.router.navigate([`projects/${this.connectionParams.projectId}/ingest/pii-identification/pgsql`]);
        }
      }, (error) => {
        if (error.ERROR_MSG) {
          this.snackbar.open(error.ERROR_MSG);
        } else {
          this.snackbar.open(error.message);
        }
        this.loading = false;
      });
  }

  // Database schema related function
  private getDatabaseSchemaList(): void {
    // Added temp changes to get data quickly
    // if (sessionStorage.getItem('schema')) {
    //   this.schemas = JSON.parse(sessionStorage.getItem('schema'));
    //   return;
    // }

    this.connectionParams.schemaName = '';
    this.connectorService.connect(this.connectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((response: any) => {
        if (response.code === 200) {
          this.schemas = response.result;
          // sessionStorage.setItem('schema', JSON.stringify(this.schemas));
        }
      });
  }

  private getTableListBySelectedSchema(tableName: string): void {
    this.connectionParams.tableName = tableName;

    this.connectorService.tables(this.connectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((response: any) => {
        this.tables = response.result;
        this.selectedTables.forEach(item => {
          const tbl = this.tables.find(x => x.name === item.tableName);
          if (tbl) {
            tbl.isClick = true;
          }
        });
      });
  }

  private getMetaDataByTableName(tableName: string): void {
    this.connectionParams.tableName = tableName;
    this.connectorService.tablesData(this.connectionParams)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((response: any) => {
        const tbl = this.tables.find(x => x.name === tableName);

        if (tbl) {
          tbl.metaData = response.result;
        }
      });
  }

  public selectionSchema(schemaName: string): void {
    this.queryType = 'default';
    this.connectionParams.schemaName = schemaName;
    if (this.schemas && this.schemas.length) {
      this.schemas.map(t => t.isClick = (t.name == schemaName) ? t.isClick = !t.isClick : false);
    }

    this.getTableListBySelectedSchema(schemaName);
  }

  public selectionTable(event: any, schema: string, tableName: string): void {
    this.queryType = 'default';
    this.connectionParams.tableName = tableName;

    for (const item of this.tables.filter(x => x.isClick)) {

      const tbl = this.selectedTables.find(x => x.tableName === item.name);
      if (!tbl) {
        this.selectedTables.push({
          isClick: item.isClick,
          schemaName: schema,
          tableName: item.name
        });
      }
    }

    if (!event.target.checked) {
      const index = this.selectedTables.findIndex(x => x.tableName === tableName);

      this.selectedTables.splice(index, 1);
    }
    else if (this.selectedTables.length > 3) {
      event.target.checked = false;
      const lastClickedTable = this.tables.find(x => x.name === tableName);

      if (lastClickedTable !== null) {
        lastClickedTable.isClick = false;

        const index = this.selectedTables.findIndex(x => x.tableName === tableName);
        this.selectedTables.splice(index, 1);
      }
      this.snackbar.open('You can select maximum 3 tables only');
      return;
    }

    this.prePareQuery();
    this.userSelectedTable = this.selectedTables.length ? this.selectedTables[0].tableName : '';
    this.getMetaDataByTableName(tableName);
  }

  public changeQrType(event: any): void {
    if (event.target.value === 'custom') {
      this.sqlType = "C";
      this.fg.controls.sqlQueryName.enable();
    } else {
      if (this.selectedTables.length) {
        this.prePareQuery();
      }
      this.fg.controls.sqlQueryName.disable();
    }
  }

  private prePareQuery(): void {
    let sql = '';

    for (const table of this.selectedTables) {
      sql += `select * from ${table.schemaName}.${table.tableName}; \n`;
    }

    sql = sql.substring(0, sql.length - 2); // remove the last added <br> tag;

    this.fg = this.fb.group({
      sqlQueryName: [`${sql}`, Validators.required]
    });
  }
}
