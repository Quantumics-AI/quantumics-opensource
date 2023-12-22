import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { SourceDataService } from '../../services/source-data.service';

@Component({
  selector: 'app-edit-data-source',
  templateUrl: './edit-data-source.component.html',
  styleUrls: ['./edit-data-source.component.scss']
})
export class EditDataSourceComponent implements OnInit {
  @Input() projectId: number;
  @Input() userId: number;
  @Input() sourceData: any;
  @Input() awsId: number;
  // @Input() rolename: string;
  public loading: boolean;
  fg: FormGroup;
  public accessType: string;
  invalidPattern: boolean = false;
  invalidCapitalPattern: boolean = false;

  public accessData:any;
  transformedData: any[] = [];
  public selectedType: string;
  public sourceListData: any;
  public alreadyExist: boolean = false;
  public pageSize: number = 1;
  public pageLength: number = 100;
  public editable: boolean = false;

  constructor(
    public modal: NgbActiveModal,
    private snakbar: SnackbarService,
    private fb: FormBuilder,
    private sourceDataService: SourceDataService,
  ){

  }

  ngOnInit(): void {
    this.getAccessTypes();
    // this.getAwsList()
    this.fg = this.fb.group({
      sourceId: [this.sourceData.id],
      dataSourceName: [this.sourceData.connectionName, [Validators.required, Validators.pattern(/^([A-Z]).([A-Za-z0-9_:-]+\s)*[A-Za-z0-9_:-]+$/)]],
      connectionType:[{ value: this.sourceData.accessType, disabled: true }],
      // roleName:[{ value: this.rolename, disabled: true }],
      subDataSource: [{ value: this.sourceData.subDataSource, disabled: true }, Validators.required],
      bucketName:[{ value: this.sourceData.bucketName, disabled: true }],
      region: [{ value: this.sourceData.region, disabled: true }]
    })
  }

  public getAccessTypes(): void {
    this.sourceDataService.getAccessTypes().subscribe((res) => {
      console.log(res);
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

  checkIfNameExists() {
    if(this.fg.get('dataSourceName').value.length <= 0){
      this.alreadyExist = false;
    }
    // if (this.sourceListData.length > 0) {
    //   const value = this.fg.get('dataSourceName').value;
    //   const titleCaseValue = value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();

    //   // const enteredName = this.fg.get('dataSourceName').value;
    //   const nameExists = this.sourceListData.some(item => item.connectionName === titleCaseValue);
      
    //   if (nameExists) {
    //     this.alreadyExist = true;
    //   } else {
    //     this.alreadyExist = false;
    //   }
    // }
    
  }

  modelChangeDataSourceName(str) {

    // const re = /^[a-zA-Z0-9_]+$/;
    this.alreadyExist = false;
    this.editable = true;
    if (this.fg.controls.dataSourceName.value != "") {

      const validCapital = String(str).match(/^([A-Z])/);
      
      if (validCapital == null) {
        this.invalidCapitalPattern = true;
      } else {
        this.invalidCapitalPattern = false;
        const validWorkSpaceName = String(str)
        .match(
          /^([A-Z]).([A-Za-z0-9_:-]+\s)*[A-Za-z0-9_:-]+$/
        );

        if (validWorkSpaceName == null) {
          this.invalidPattern = true;

        } else {
          this.invalidPattern = false;
        }
      }
    } else {
      this.invalidPattern = false;
      this.invalidCapitalPattern = false;
    }

  }

  public checkExistName(): void {
    const checkD = this.sourceData.connectionName.toLowerCase() === this.fg.get('dataSourceName').value.toLowerCase();
    
    if(checkD) {
      // this.snakbar.open("No change in the name");
      // this.modal.close();
      this.updateData();
    } else {
      const name = this.fg.get('dataSourceName').value;
      this.sourceDataService.getIsExistConnection(this.userId, this.projectId, name, this.pageSize, this.pageLength).subscribe((response) => {
        
        console.log(response.isExist);
        if(response.isExist){
          this.alreadyExist = true;
          
        } else {
          this.alreadyExist = false;
          this.updateData();
        }
        
      }, (error) => {
  
      });
    }
    
  }

  public updateData(): void {
    // const value = this.fg.get('dataSourceName').value;
    // const titleCaseValue = value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
    // this.fg.get('dataSourceName').setValue(titleCaseValue);
    
    const request = {
      projectId: this.projectId,
      userId: this.userId,
      connectionName: this.fg.controls.dataSourceName.value,
      accessType: this.sourceData.accessType,
      // connectionData: this.sourceData.connectionData,
      subDataSource: this.fg.controls.subDataSource.value,
      bucketName: this.fg.controls.bucketName.value,
      region: this.fg.controls.region.value,
    }

    this.sourceDataService.updateSourceData(this.awsId, request).subscribe((response) => {
      // if (response.code === 200) {
        this.modal.close(response);
        this.snakbar.open(response.message);
      // }
    }, (error) => {
      this.snakbar.open(error);
      this.modal.close();
    });
    // this.snakbar.open("Data source updated successfully!")
    // this.modal.close();
  }
}
