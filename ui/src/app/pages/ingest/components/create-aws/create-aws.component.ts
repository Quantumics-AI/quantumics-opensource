import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Location } from '@angular/common';
import { AwsData, RoleData } from '../../models/awsdata';
import { SourceDataService } from '../../services/source-data.service';

@Component({
  selector: 'app-create-aws',
  templateUrl: './create-aws.component.html',
  styleUrls: ['./create-aws.component.scss']
})
export class CreateAwsComponent {
  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject();
  public projectId: number;
  public awsData: AwsData;
  public userId: number;

  public fg: FormGroup;
  public loading: boolean;
  public queryFolders: boolean = true;
  public selectedPolicyName: string;
  public policyData: any = [
    {
      id: 1,
      policyName: "IAM",
    },
    {
      id: 2,
      policyName: "Resource Policy",
    },
    {
      id: 3,
      policyName: "Secret Key / Access Key"
    }
  ];
  public connection: boolean = false;
  invalidPattern: boolean = false;
  invalidCapitalPattern: boolean = false;
  public connectionParams: AwsData;
  public accessData: any;
  transformedData: any[] = [];
  public sourceListData: any;
  public alreadyExist: boolean = false;
  public connectionData: any;
  public pageSize: number = 1;
  public pageLength: number = 100;
  public selectedBucket: string;
  public bucketList: any;
  public regionList: any;

  constructor(
    private fb: FormBuilder,
    private snakbar: SnackbarService,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private router: Router,
    private location: Location,
    private sourceDataService: SourceDataService,
  ){
    // this.awsData = new AwsData();
    this.connectionData = JSON.parse(sessionStorage.getItem('awsData'));
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        if (certificate) {
          this.certificateData = certificate;
          this.userId = +this.certificateData.user_id;
        }
      });
  }

  ngOnInit(): void {
    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
      //End space allowed    /^([A-Z]).*([A-Za-z0-9_:-]+\s*)*[A-Za-z0-9_:-]+\s*$/
    this.fg = this.fb.group({
      dataSourceName: ['', [Validators.required, Validators.pattern(/^[A-Z]([A-Za-z0-9_:-]+\s)*[A-Za-z0-9_:-]*$/)]],
      connectionType: [{ value: 'PROFILE', disabled: true }],
      bucket: ['', [Validators.required]],
      region: ['', [Validators.required]],
      subDataSource: [{ value: 'S3', disabled: true }, Validators.required],
      // roleName: ['Shivraj', Validators.required]
    });

    this.getAccessTypes();
    // this.getAwsList()
    // this.getBucketData();
    this.getRegionList();
  }

  ngAfterViewInit(): void {
    // if (this.connectionData) {
    //   this.fg.controls.dataSourceName.setValue(this.connectionData.connectionName);
    //   this.connection = true;
    // }
  }

  public getBucketData(): void {
    this.sourceDataService.getBucketData(this.userId, this.projectId).subscribe((res) => {
      this.bucketList = res;
    }, (error) => {
      this.snakbar.open(error);
    });
  }

  public getRegionList(): void {
    this.sourceDataService.getRegionList(this.userId, this.projectId).subscribe((res) => {
      console.log("==", res);
      this.regionList = res;
    }, (error) => {
      this.snakbar.open(error);
    });
  
  }
  // public getAwsList(): void {
  //   this.loading = true;
  //   this.sourceDataService.getSourceData(+this.projectId, this.userId).subscribe((response) => {
  //     this.loading = false;
  //     this.sourceListData = response;
  //     if (this.sourceListData.length > 1) {
  //       this.sourceListData.sort((val1, val2) => {
  //         return (
  //           (new Date(val2.createdDate) as any) -
  //           (new Date(val1.createdDate) as any)
  //         );
  //       });
  //     }
  //   }, (error) => {
  //     this.loading = false;
  //   })
  // }

  public checkExistName(): void {
    const name = this.fg.get('dataSourceName').value;
    this.sourceDataService.getIsExistConnection(this.userId, this.projectId, name, this.pageSize, this.pageLength).subscribe((response) => {
      
      console.log(response.isExist);
      if(response.isExist){
        this.alreadyExist = true;
        
      } else {
        this.alreadyExist = false;
        this.testConnection();
      }
      
      // if (response.isExist) {
      //   this.alreadyExist = true;
      // } else {
      //   this.alreadyExist = false;
      // }
      
    }, (error) => {

    });
  }

  checkIfNameExists() {
    this.connection = false;
    if(this.fg.get('dataSourceName').value.length <= 0){
      this.alreadyExist = false;
    }

    // if (this.sourceListData.length > 0) {
    //   const value = this.fg.get('dataSourceName').value;
    //   // const titleCaseValue = value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();

    //   // const enteredName = this.fg.get('dataSourceName').value;
    //   const nameExists = this.sourceListData.some(item => item.connectionName === value);
      
    //   if (nameExists) {
    //     this.alreadyExist = true;
    //   } else {
    //     this.alreadyExist = false;
    //   }
    // }
    
  }

  public onSelectRegion(str: string): void {
    this.connection = false;
    // const selectedRegion = this.bucketList.find(i => i.name == str);
    // this.fg.controls.region.setValue(selectedRegion?.region);
  }


  checkBucket() {
    this.connection = false;
  }

  public getAccessTypes(): void {
    this.sourceDataService.getAccessTypes().subscribe((res) => {
      this.accessData = res;
      this.transformData();
    }, (error) => {

    });
  }

  public transformData() {
    let id = 1;
    for (const key in this.accessData) {
      if (this.accessData.hasOwnProperty(key)) {
        const type = this.accessData[key];
        this.transformedData.push({ id, type });
        id++;
      }
    }

    console.log(this.transformedData);
    
  }


  public onSelectAcceptType(type: any): void {
    // const obj = this.transformedData.find(x => x.type === 'PROFILE');
    // if (obj) {
    //   this.fg.controls.connectionType.setValue(obj.type);
    // }
  }

  modelChangeDataSourceName(str) {
    // const re = /^[a-zA-Z0-9_]+$/;  /^([A-Z]).([A-Za-z0-9_:-]+\s)*[A-Za-z0-9_:-]+$/

    this.alreadyExist = false;
    this.connection = false; 
    const ruleNameControl = this.fg.controls.dataSourceName;

    if (ruleNameControl.value !== "") {
        const validCapital = String(str).match(/^([A-Z])/);

        if (validCapital == null) {
            this.invalidCapitalPattern = true;
            this.invalidPattern = false;
        } else {
            this.invalidCapitalPattern = false;

            if (str.length < 3) {
              const validWorkSpaceName = String(str)
              .match(/^[A-Z]([A-Za-z0-9_:-]+\s)*[A-Za-z0-9_:-]*$/);

              if (validWorkSpaceName == null) {
                  this.invalidPattern = true;
              } else {
                  this.invalidPattern = false;
              }
            } else {
                const validWorkSpaceName = String(str)
                    .match(/^[A-Z]([A-Za-z0-9_:-]+\s)*[A-Za-z0-9_:-]*$/);

                if (validWorkSpaceName == null) {
                    this.invalidPattern = true;
                } else {
                    this.invalidPattern = false;
                }
            }
        }
    } else {
        this.invalidPattern = false;
        this.invalidCapitalPattern = false;
    }

    // Trigger the validation manually to update the error status
    ruleNameControl.updateValueAndValidity({ onlySelf: true, emitEvent: false });

  }

  public testConnection(): void {

    // const role_data = {
    //   iam: this.fg.controls.roleName.value,
    // } as RoleData;

    // const value = this.fg.get('dataSourceName').value;
    // const titleCaseValue = value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
    // this.fg.get('dataSourceName').setValue(titleCaseValue);

    this.connectionParams = {
      projectId: this.projectId,
      userId: this.userId,
      connectionName: this.fg.controls.dataSourceName.value,
      accessType: this.fg.controls.connectionType.value,
      subDataSource: this.fg.controls.subDataSource.value,
      bucketName: this.fg.controls.bucket.value,
      region: this.fg.controls.region.value
    } as AwsData;
    console.log(this.connectionParams);

    this.sourceDataService.testConnection(this.userId,this.projectId, this.connectionParams).subscribe((response) => {
      
      sessionStorage.setItem('awsData', JSON.stringify(this.connectionParams));
      this.snakbar.open(response.message);
      this.connection = true;
    }, (error) => {
      this.snakbar.open(error);
      this.connection = false;
    });
    
  }

  public continue(): void {
    // const name = this.fg.get('dataSourceName').value;
    // this.sourceDataService.getIsExistConnection(this.userId, this.projectId, name, this.pageSize, this.pageLength).subscribe((response) => {
      
    //   console.log(response.isExist);
    //   if(response.isExist){
    //     this.alreadyExist = true;
    //     this.connection = false;
        
    //   } else {
    //     this.alreadyExist = false;
    //     this.connection = true;
    //     this.connectionParams = {
    //       projectId: this.projectId,
    //       userId: this.userId,
    //       connectionName: this.fg.controls.dataSourceName.value,
    //       accessType: this.fg.controls.connectionType.value,
    //       subDataSource: this.fg.controls.subDataSource.value,
    //       bucketName: '',
    //     } as AwsData;
    //     sessionStorage.setItem('awsData', JSON.stringify(this.connectionParams));
    //     this.router.navigate([`projects/${this.projectId}/ingest/select-source-target`]);
    //   }
      
    // }, (error) => {

    // });
    this.sourceDataService.saveSourceData(this.connectionParams).subscribe((response) => {
      console.log("API", response);
      this.snakbar.open("New connection created successfully");
      this.router.navigate([`projects/${this.projectId}/ingest/aws`]);
    }, (error) => {
      this.snakbar.open(error);
    })
    
  }

  public back(): void {
    this.location.back();
  }
}
