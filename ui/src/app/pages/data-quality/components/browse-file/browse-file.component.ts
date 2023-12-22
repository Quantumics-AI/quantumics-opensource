import { Component, OnInit, EventEmitter, Output, Input } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { retry, takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { RuleCreationService } from '../../services/rule-creation.service';
import { Location } from '@angular/common';

@Component({
  selector: 'app-browse-file',
  templateUrl: './browse-file.component.html',
  styleUrls: ['./browse-file.component.scss']
})
export class BrowseFileComponent implements OnInit {

  loading: boolean;
  public isDescending: boolean;
  projectId: string;
  userId: number;
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  private certificateData: Certificate;

  public fileStructure: any;
  // {
  //   Aggregatefolder: {
  //     '213': {
  //       files: [
  //         '_SUCCESS',
  //         'part-00000-d9eef3f9-1a84-4e40-886f-f9ed12973aa6-c000.csv',
  //       ],
  //       anotherNode: {
  //         someFile: {},
  //       },
  //     },
  //     sunil: {
  //       sunil01: {},
  //     },
  //   },
  //   SecondFolder: {},
  //   ThirdFolder: {
  //     NestedFolder: {
  //       files: [
  //         'part-00000-d9eef3f9-1a84-4e40-886f-f9ed12973aa6-c000.csv'
  //       ]
  //     },
  //     files: [
  //       'part-00000-d9eef3f9-1a84-4e40-886f-f9ed12973aa6-c000.csv'
  //     ]
  //   },
  // };

  selectedNode: string;
  openNodes: string[] = [];

  public headers: any;
  public fileContent: any;
  public columnType: any;
  public rowCount: number;
  public totalRowCount: any;

  public childData: any;
  public isShowTable: boolean = false;
  public bucketName: string;
  public selectedFile: string;
  public filePath: string;
  public patternPath: string;
  public selectedSource: string;
  public regionName: string;
  public accesType: string;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private snakbar: SnackbarService,
    private ruleCreationService: RuleCreationService,
    private location: Location,
  ){
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        if (certificate) {
          this.certificateData = certificate;
          this.userId = +this.certificateData.user_id;
        }
      });
      this.selectedSource = sessionStorage.getItem('source');
      this.regionName = sessionStorage.getItem('region');
      this.accesType = sessionStorage.getItem('accessType');
  }

  ngOnInit(): void {
    // getPipelineData
    this.projectId = localStorage.getItem('project_id');
    this.bucketName = sessionStorage.getItem('bucketName');
    this.getBrowseFileData();
  }

  public getBrowseFileData(): void {
    this.loading = true;
    this.ruleCreationService.getBrowseFile(this.userId, +this.projectId, this.bucketName, this.regionName, this.accesType).subscribe((res) => {
      
      this.fileStructure = res;
      this.loading = false;
    }, (error) => {
      this.snakbar.open(error);
      this.loading = false;
    })
  }

  getObjectKeys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }

  isObject(obj: any): boolean {
    return typeof obj === 'object' && !Array.isArray(obj);
  }

  isArray(obj: any): boolean {
    return Array.isArray(obj);
  }

  convertKBtoMB(kb: number): number {
    return kb / 1024; // 1 MB = 1024 KB
  }

  toggleNode(node: string, indentLevel: number): void {
    if (indentLevel === 0) {
      this.openNodes = [];
    }

    if (indentLevel === 1) {
      for (let i = 1; i <= this.openNodes.length; i++) {
        this.openNodes.splice(i, 1);
      }
    }

    if (this.selectedNode === node) {
      this.selectedNode = null;
    } else {
      this.selectedNode = node;
    }

    // Toggle the node's open/closed state
    const index = this.openNodes.indexOf(node);

    if (index !== -1) {
      this.openNodes.splice(index, 1);
    } else {
      this.openNodes.push(node);
    }

    // console.log(this.openNodes);
    
  }

  isNodeOpen(node: string): boolean {
    return this.openNodes.includes(node);
  }

  public getTableFile(d: any): void {
    // this.tableData.map(t => {
    //   if(t.id == d.id){
    //     t.expanded = !t.expanded;
    //     console.log("expanded data:",t);
    //     this.childData = t.children;
    //     console.log("expanded child:",this.childData);
    //   } else {
    //     t.expanded = false;
    //   }
    // })
  }
  
  public formatFileSize(bytes): string {
    if (bytes < 1024) {
      return bytes + " bytes";
    } else if (bytes < 1024 * 1024) {
      return (bytes / 1024).toFixed(2) + " KB";
    } else if (bytes < 1024 * 1024 * 1024) {
      return (bytes / (1024 * 1024)).toFixed(2) + " MB";
    } else if (bytes < 1024 * 1024 * 1024 * 1024) {
      return (bytes / (1024 * 1024 * 1024)).toFixed(2) + " GB";
    } else {
      return (bytes / (1024 * 1024 * 1024 * 1024)).toFixed(2) + " TB";
    }
  }

  public showTable(file: any): void {
    // debugger
    this.selectedFile = file.fileName;
    const index = this.openNodes.indexOf(file);
    if (index !== -1) {
      this.openNodes.splice(index, 1);
    } else {
      this.openNodes.push(file.fileName);
    }

    // console.log('show table', file.fileName , 'open nodes:', this.openNodes );
    console.log('path',this.openNodes.join('/') );
    const stringD = `s3://${this.bucketName}/${this.openNodes.join('/')}`;
    console.log("PatternPath:", stringD);

    this.filePath = this.openNodes.join('/');

    // set pattern path 
    // pattern format - s3://BUCKET_NAME/FEED_NAME/DDMMYYYY/FILENAME
    this.patternPath = `s3://${this.bucketName}/${this.filePath}`;
    // myArray.join('/')
    this.getFileData();
    // this.getRowCount();
    
  }

  public getFileData(): void {
    this.loading = true;
    this.ruleCreationService.getFileContent(this.userId, +this.projectId, this.bucketName, this.filePath, this.regionName, this.accesType).subscribe((res) => {
      
      this.headers = res.headers;
      this.fileContent = res.content;
      this.columnType = res.columnDatatype;
      this.rowCount = res?.rowCount;
      this.loading = false;
      if (this.rowCount == 500) {
        this.getRowCount();
      } else {
        this.totalRowCount = this.rowCount;
      }
      
      this.isShowTable = !this.isShowTable;
      // this.filePath = "";
      this.openNodes = [];
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
      this.filePath = "";
      this.openNodes.pop();
    })
  }

  
  public getRowCount(): void {
    this.totalRowCount = '';
    this.ruleCreationService.getFileRowCount(this.userId, +this.projectId, this.bucketName, this.filePath).subscribe((res) => {
      console.log("Row data", res);
      const parsedObject = JSON.parse(res.message);
      this.totalRowCount = parsedObject?.rowCount;
      console.log("Row data", this.totalRowCount);
    }, (error) => {
      this.snakbar.open(error);
    })
  }

  public back(): void {
    this.location.back();
  }
}
