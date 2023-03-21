import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { takeUntil } from 'rxjs/operators';
import { Observable, Subject, forkJoin } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';
import { NgxSpinnerService } from 'ngx-spinner';
import { CleansingDataService } from './services/cleansing-data.service';
import { CleansingService } from './services/cleansing.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { StandardizeComponent } from './components/standardize/standardize.component';
import { decrypt } from 'src/app/core/utils/decryptor';

@Component({
  selector: 'app-cleansing',
  templateUrl: './cleansing.component.html',
  styleUrls: ['./cleansing.component.scss']
})

export class CleansingComponent implements OnInit, OnDestroy {
  rules: any;
  selectedRule: any;
  originalContent: Array<any>;
  columnsMetadata: Array<any>;
  projectId: any;
  folderId: string;
  file: any;
  fileId: number;
  loading = false;
  data: any;
  folders: any;
  graphData = [];
  files = [];
  newColumns = [];

  public piiColumns: Array<string>;

  rowLength: any;
  columnLength: any;
  loaded$: Observable<boolean>;
  sourceData$: Observable<any>;
  jobStatus = false;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  projectParam: ProjectParam;
  folderName: string;

  private unsubscribe: Subject<void> = new Subject();
  uniqueDatatypes: number;
  public editorComponent: any;
  public ruleInputLogic1: string;
  public hasRulesCatalogue: boolean = false;

  showPreview: boolean;
  projectName: string;
  showFullView = true;
  constructor(
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private router: Router,
    private cleansingDataService: CleansingDataService,
    private snackBar: SnackbarService,
    private spinner: NgxSpinnerService,
    private cleansingService: CleansingService,
    private modalService: NgbModal
  ) {
    this.sourceData$ = this.quantumFacade.sourceData$;
    this.loaded$ = this.quantumFacade.loaded$;
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
      });


    this.projectId = this.activatedRoute.snapshot.paramMap.get('projectId');
    this.folderId = this.activatedRoute.snapshot.paramMap.get('folderId');
    this.fileId = +this.activatedRoute.snapshot.paramMap.get('fileId');

    this.folderName = localStorage.getItem('cleansing-selected-folder-name');
    this.file = localStorage.getItem('cleansing-selected-file-name');

    this.loadData();
  }
  ngOnInit(): void {
    this.loading = true;
    this.projectName = localStorage.getItem('projectname');
    this.projectParam = {
      userId: this.certificateData.user_id,
      folderId: this.folderId,
      folderName: this.folderName,
      projectId: this.projectId,
      file: this.file
    };

    this.sourceData$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((response: any) => {
        if (response) {
          this.files = response.result;
        }
      });
  }

  getFiles(folderId, folderName) {
    this.folderName = folderName;
    this.folderId = folderId;
    this.projectParam.folderId = this.folderId;
    this.projectParam.projectId = this.projectId;
    this.quantumFacade.loadSourceData(this.projectParam);
    this.sourceData$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((data: any) => {
        if (data) {
          if (data.result) {
            this.files = data.result;
          } else {
            this.files = [];
          }
        }
      });
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  loadData(): void {
    const params = {
      userId: this.certificateData.user_id,
      projectId: this.projectId,
      folderId: this.folderId,
      folderName: this.folderName,
      fileId: this.fileId,
      file: this.file
    };

    const ob1 = this.cleansingDataService.getFileContent(params);
    const ob2 = this.cleansingDataService.getRules(params);

    this.spinner.show();
    forkJoin([ob1, ob2]).subscribe((res) => {
      this.parseRulesData(res[1]);
      this.parseFileData(res[0]);
      this.loading = false;
    }, () => {
      this.loading = false;
    });
  }

  updateData(fullData: any): void {
    this.data = fullData.data;
    this.newColumns = fullData.metadata;
  }

  getRules(): void {
    const params = {
      userId: this.certificateData.user_id,
      projectId: this.projectId,
      folderId: this.folderId,
      folderName: this.folderName,
      fileId: this.fileId,
      file: this.file
    };
    this.cleansingDataService.getRules(params).subscribe(res => {
      this.parseRulesData(res);
      this.reapplyRules();
    });
  }


  parseFileData(response): void {
    const data = JSON.parse(decrypt(response.data));
    const metadata = JSON.parse(decrypt(response.metadata));

    let temp;
    try {
      temp = JSON.parse(response.file_additional_info)?.encryptPiiColumns?.split(',');
    } catch (error) {
      // Ignore
    }

    this.piiColumns = temp ? temp : [];
    this.originalContent = data.map(r => ({ ...r }));
    this.columnsMetadata = metadata.map(r => ({ ...r }));

    const columnsMetadata = metadata;
    if (this.rules && this.rules?.length > 0) {
      const res = this.cleansingService.apply(this.rules, data, columnsMetadata);
      this.data = res.data;
      this.newColumns = res.columns;
    } else {
      this.data = data;
      this.newColumns = metadata;
    }

    this.columnLength = this.newColumns.length;
    this.rowLength = this.data.length;
  }

  parseRulesData(response): void {
    if (response?.code === 200) {
      this.rules = response.result;
      this.hasRulesCatalogue = true;
    } else {
      this.rules = [];
      this.hasRulesCatalogue = false;
    }

    this.updateView();
  }

  viewChange(d): void {
    this.showFullView = !d.editorOpen;
    this.selectedRule = undefined;
    this.editorComponent = d.editorComponent;
    this.ruleInputLogic1 = d.ruleInputLogic1;

    if (this.editorComponent === 'standardize') {
      this.openStandardizeModal();
    }
  }


  private openStandardizeModal(rule?): void {
    const modalRef = this.modalService.open(StandardizeComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.rows = this.data;
    modalRef.componentInstance.columns = this.newColumns;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.folderId = this.folderId;
    modalRef.componentInstance.fileId = this.fileId;

    if (rule) {
      modalRef.componentInstance.newValue = rule?.ruleInputValues;
      modalRef.componentInstance.selectedColumn = rule?.ruleImpactedCols;
      modalRef.componentInstance.ruleInputValues1 = rule?.ruleInputValues1;
      modalRef.componentInstance.ruleSequence = rule?.ruleSequence;
      modalRef.componentInstance.cleansingParamId = rule?.cleansingParamId;
      modalRef.componentInstance.isUpdate = true;

    }

    modalRef.result.then((r) => {
      this.addRule(r);
    }, (reason) => {
      // when modal is dismissed -> modal.dismiss
      this.updateView();
      console.log(reason);
    });
  }

  redirectToFolder(): void {
    this.router.navigate([`projects/${this.projectId}/cleansing`]);
  }

  edit(rule): void {
    this.editorComponent = rule.ruleInputLogic;

    if (this.editorComponent === 'standardize') {
      this.openStandardizeModal(rule);
    } else {
      this.selectedRule = rule;
      rule = Object.assign({}, rule);

      const tempRules = this.rules.filter(r => (r.cleansingParamId !== rule.cleansingParamId));
      const columnsMetadata = this.columnsMetadata.map(r => ({ ...r }));
      const tempData = this.originalContent.map(r => ({ ...r }));
      if (tempRules?.length > 0) {
        const res = this.cleansingService.apply(
          tempRules,
          tempData,
          columnsMetadata
        );

        this.data = res.data;
        this.newColumns = res.columns;
      } else {
        this.data = tempData;
        this.newColumns = columnsMetadata;
      }

      rule.ruleImpactedCols = rule.ruleImpactedCols.split(',');

      this.previewRuleNew(rule);
    }
  }

  public sequenceChanged(): void {
    this.getRules();
  }

  addRule(rule: any): void {
    this.removePreviewColumns();

    this.showPreview = false;
    rule.projectId = this.projectId;
    rule.fileId = this.fileId;
    rule.folderId = this.folderId;
    rule.ruleSequence = rule.ruleSequence ? rule.ruleSequence : 0;
    rule.cleansingParamId = rule.cleansingParamId ? rule.cleansingParamId : 0;
    rule.cleansingRuleId = 0;
    rule.userId = this.certificateData.user_id;

    const apiResponse = rule.update ? this.cleansingDataService.updateRules(rule) : this.cleansingDataService.addRules(rule);

    apiResponse.subscribe((response: any) => {
      if (response.code === 200) {
        this.getRules();
        this.snackBar.open(response.message);
      } else {
        this.snackBar.open(response.message);
      }
    });
  }

  ruleDeleted(ruleId: number): void {
    this.getRules();
  }

  private reapplyRules() {
    const tempData = this.originalContent.map(r => ({ ...r }));
    const metadata = this.columnsMetadata.map(r => ({ ...r }));


    if (this.rules?.length > 0) {
      const res = this.cleansingService.apply(this.rules, tempData, metadata);

      this.data = res.data;
      this.newColumns = res.columns;

    } else {
      this.data = tempData;
      this.newColumns = metadata;
    }

    this.columnLength = this.newColumns.length;
    this.rowLength = this.data.length;
  }

  previewRuleNew(rule: any): void {
    this.removePreviewColumns();

    rule.ruleImpactedCols = rule.ruleImpactedCols.join(',');
    const res = this.cleansingService.preview(rule, this.data, this.newColumns);

    this.showPreview = true;

    this.data = res.data;
    this.newColumns = res.columns;
  }

  cancelPreview(reapplyRules: boolean): void {
    this.showPreview = false;
    this.updateView();
    this.removePreviewColumns();
    this.reapplyRules();
  }

  rowPreview(row: any): string {
    if (row.preview__row) {
      return '#FFFFE0';
    } else {
      return '';
    }
  }

  private removePreviewColumns(): void {
    // remove preview field from columns
    this.newColumns = this.newColumns.filter(el => el.preview !== 'new');
    this.newColumns.forEach(col => {
      delete col.preview;
    });

    // remove preview rows if any
    this.data.forEach(row => {
      delete row.preview__row;
    });
  }

  private updateView(): void {
    if (this.rules.length > 0) {
      this.editorComponent = 'rules';
      this.showFullView = false;
    } else {
      this.editorComponent = undefined;
      this.showFullView = true;
    }
  }

  back(): void {
    this.router.navigate([`projects/${this.projectId}/cleansing`]);
  }

  public isPIIColumn(columnName: string): boolean {
    return this.piiColumns.includes(columnName);
  }
}
