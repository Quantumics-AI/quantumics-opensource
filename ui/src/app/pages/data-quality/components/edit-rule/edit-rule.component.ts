import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Location } from '@angular/common';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { RuleCreationService } from '../../services/rule-creation.service';
import { StatusDeleteConfirmationComponent } from '../status-delete-confirmation/status-delete-confirmation.component';


function alwaysInvalid(control: AbstractControl): { alwaysInvalid: true } {
  return { alwaysInvalid: true };
}

function acceptancePercentageValidator(control: FormControl) {
  const inputValue = control.value;
  // Convert inputValue to string if it's not
  const inputValueString = typeof inputValue === 'string' ? inputValue : inputValue.toString();
  const replacedValue = inputValueString.replace(/[^0-9]/g, '');
  if (replacedValue === null || replacedValue === '') {
    return { emptyPercentage: true }; // Accept empty input
  }

  // Replace non-numeric characters
  // const replacedValue = inputValueString.replace(/[^0-9]/g, '');

  // Check if the replaced value is a whole number (no decimals)
  // debugger
  if (!/^[0-9]+$/.test(replacedValue)) {
    return { invalidPercentage: true };
  }

  const numericValue = parseInt(replacedValue, 10);
  if (isNaN(numericValue) || numericValue < 0 || numericValue > 20) {
    return { invalidPercentage: true };
  }

  return null;
}

@Component({
  selector: 'app-edit-rule',
  templateUrl: './edit-rule.component.html',
  styleUrls: ['./edit-rule.component.scss']
})
export class EditRuleComponent implements OnInit {
  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject();
  public fg: FormGroup;
  public sourceTargetType: boolean;
  public isShowData: boolean = false;
  public projectId: number;
  userId: number;
  public selectedNullName: string;
  public completenessData: any = [];
  public ruleTypesData: any;
  public selectedMainRuleType: string;
  public selectedSubLevel: string;
  invalidPattern: boolean = false;
  invalidCapitalPattern: boolean = false;
  invalidDesPattern: boolean = false;
  public dataConnectionList: any;
  public selectedDataConnections1: string;
  public selectedDataConnections2: string;
  public selectedAttributeName: string;
  public selectedMultipleAttributeName: any =[];
  public bucketList: any;
  public selectedBucketOne: string;
  public selectedBucketTwo: string;
  public connectionParams: any;
  public bucketSourceOne: any = [];
  public bucketSourceTwo: any = [];
  public saveRulePayload: any;
  public ruleId: number;
  public fetchEditRule: any;
  public ruleFilter = [
    { label: 'Active', name: 'Active', selected: false },
    { label: 'Inactive', name: 'Inactive', selected: false },
    { label: 'Delete', name: 'Deleted', selected: false }
  ];
  public loading: boolean
  public ruleStatus: string = 'Active';
  public pageNumebr: number = 1;
  public pageLength: number = 100;
  public alreadyExist: boolean = false;
  public getRuleList: any;
  public statusBody: any = ["Active","Inactive"];
  public connectionPage: number = 1;
  public connectionPageSize: number = 10000;
  public selectedStatusValue: string;
  public editableForm: boolean = false;
  public selectedDaysString: string;
  public selectedDays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
  public ruleDays = [
    {id: 1, day: 'Mon', label: 'Mon', isChecked: false},
    {id: 2, day: 'Tue', label: 'Tue', isChecked: false},
    {id: 3, day: 'Wed', label: 'Wed', isChecked: false},
    {id: 4, day: 'Thu', label: 'Thu', isChecked: false},
    {id: 5, day: 'Fri', label: 'Fri', isChecked: false},
    {id: 6, day: 'Sat', label: 'Sat', isChecked: false},
    {id: 7, day: 'Sun', label: 'Sun', isChecked: false}
  ];

  constructor(
    private fb: FormBuilder,
    private snakbar: SnackbarService,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private router: Router,
    private location: Location,
    private ruleCreationService: RuleCreationService,
    private modalService: NgbModal,) {
      this.certificate$ = this.quantumFacade.certificate$;
      this.certificate$
        .pipe(takeUntil(this.unsubscribe))
        .subscribe(certificate => {
          if (certificate) {
            this.certificateData = certificate;
            this.userId = +this.certificateData.user_id;
          }
        });

        this.activatedRoute.queryParams.subscribe(params => {
          this.ruleId = +params.dataId;
        });
  }

  ngOnInit(): void {
    this.loading = true;
    const ruleDescriptionPattern = /^[A-Za-z0-9\s!@#$%^&*()\-_+=\[\]{}|;:'",.<>?/~`\\]*$/;
    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
    this.fg = this.fb.group({ 
      // '^[A-Za-z\\s!@#$%^&*()\\-_+=\\[\\]{}|;:\'",.<>?/]*$'
      ruleName: ['', [Validators.required, Validators.pattern(/^([A-Z]).([A-Za-z0-9_:-]+\s)*[A-Za-z0-9_:-]+$/)]],
      ruleDescription: ['', [Validators.required, Validators.pattern(ruleDescriptionPattern)]],
      sourceAndTarget: [{ value: '', disabled: true }],
      //source 
      //source1 - source2
      sourceDataSource: new FormControl({ value: 'aws', disabled: true }),
      subDataSourceOne: new FormControl({ value: 's3', disabled: true }),
      sourceDataConnection: new FormControl({ value: '', disabled: true }),
      sourceBucketOne: new FormControl({ value: '', disabled: true }),
      region: new FormControl({ value: '', disabled: true }),
      sourceFolderPath: new FormControl({ value: '', disabled: true }),
      sourcePatternPath: new FormControl({ value: 's3://BUCKET_NAME/FEED_NAME/YYYYMMDD/FILENAME', disabled: true }),
      //
      sourceDataSourceTwo: new FormControl({ value: 'aws', disabled: true }),
      subDataSourceTwo: new FormControl({ value: 's3', disabled: true }),
      sourceDataConnectionTwo: new FormControl({ value: '', disabled: true }),
      sourceBucketTwo: new FormControl({ value: '', disabled: true }),
      regionTwo: new FormControl({ value: '', disabled: true }),
      sourceFolderPathTwo: new FormControl({ value: '', disabled: true }),
      sourcePatternPathTwo: new FormControl({ value: 's3://BUCKET_NAME/FEED_NAME/YYYYMMDD/FILENAME', disabled: true }),
      //types
      ruleType: new FormControl({ value: '', disabled: true }),
      subLavelRadio: new FormControl({ value: '', disabled: true }),
      selectColumnAttribute: new FormControl({ value: '', disabled: true }),
      selectMultipleAttribute: new FormControl({ value: '', disabled: true }),
      percentage: new FormControl({ value: '', disabled: false }, [Validators.pattern('^[0-9]*$'),acceptancePercentageValidator,]),
      //
      status: new FormControl('', Validators.required),
      dummyValidator: ['', [alwaysInvalid]],
      //Rule configuration
      ruleRunDays: new FormControl('', Validators.required)
    });

    this.getDataConnection();
    this.getBucketData();
    // this.getRules();
    // this.fg.invalid == true;
  }

  ngAfterViewInit(): void {
    this.getSelectedRule();
  }

  public getSelectedRule(): void {
    this.ruleCreationService.getEditRule(this.userId, this.projectId, this.ruleId).subscribe((response) => {
      this.fetchEditRule = response.result;
      
      this.fg.controls.ruleName.setValue(this.fetchEditRule?.ruleName);
      this.fg.controls.ruleDescription.setValue(this.fetchEditRule?.ruleDescription);
      this.fg.controls.sourceAndTarget.setValue(this.fetchEditRule?.sourceAndTarget);
      if(response.result.sourceAndTarget) {
        //source1-
        this.fg.controls.sourceDataConnection.setValue(this.fetchEditRule?.sourceData.dataSourceId);
        this.fg.controls.sourceBucketOne.setValue(this.fetchEditRule?.sourceData.bucketName);
        this.selectedBucketOne = this.fetchEditRule?.sourceData.bucketName;
        this.bucketSourceOne.push(this.fetchEditRule?.sourceData.bucketName);
        this.fg.controls.region.setValue(this.fetchEditRule?.sourceData.region);
        this.fg.controls.sourceFolderPath.setValue(this.fetchEditRule?.sourceData.filePath);
        //source-2
        this.selectedDataConnections2 = this.fetchEditRule?.targetData.dataSourceId;
        this.fg.controls.sourceDataConnectionTwo.setValue(this.fetchEditRule?.targetData.dataSourceId);
        this.fg.controls.sourceBucketTwo.setValue(this.fetchEditRule?.targetData.bucketName);
        this.selectedBucketTwo = this.fetchEditRule?.targetData.bucketName;
        this.bucketSourceTwo.push(this.fetchEditRule?.targetData.bucketName);
        this.fg.controls.regionTwo.setValue(this.fetchEditRule?.sourceData.region);
        this.fg.controls.sourceFolderPathTwo.setValue(this.fetchEditRule?.targetData.filePath);
      } else {
        //source
        this.fg.controls.sourceDataConnection.setValue(this.fetchEditRule?.sourceData.dataSourceId);
        this.fg.controls.sourceBucketOne.setValue(this.fetchEditRule?.sourceData.bucketName);
        this.bucketSourceOne.push(this.fetchEditRule?.sourceData.bucketName);
        this.fg.controls.region.setValue(this.fetchEditRule?.sourceData.region);
        this.fg.controls.sourceFolderPath.setValue(this.fetchEditRule?.sourceData.filePath);
      }
      this.getRuleTypeList();
      // status
      this.fg.controls.status.setValue(this.fetchEditRule?.status);
      this.fg.controls.percentage.setValue(this.fetchEditRule?.ruleDetails.ruleLevel.acceptance);

      //Rule configuration
      this.selectedDaysString = this.fetchEditRule?.ruleRunDays;
      this.fg.controls.ruleRunDays.setValue(this.selectedDaysString);
      // this.fg.controls.status.setValue(result?.status)
      // this.fg.controls.synonymTerm.setValue(result?.synonymTerm);
      //Types
      setTimeout(() => {
        this.selectedMainRuleType = this.fetchEditRule?.ruleDetails.ruleTypeName;
        this.fg.controls.ruleType.setValue(this.fetchEditRule?.ruleDetails.ruleTypeName);
        this.fg.controls.subLavelRadio.setValue(this.fetchEditRule?.ruleDetails.ruleLevel.levelName);
        this.selectedSubLevel = this.fetchEditRule?.ruleDetails.ruleLevel.levelName;
        if(this.fetchEditRule?.ruleDetails.ruleLevel.levelName != 'Multiple Columns') {
          this.fg.controls.selectColumnAttribute.setValue(this.fetchEditRule?.ruleDetails.ruleLevel.columns[0]);
          this.completenessData.push(this.fetchEditRule?.ruleDetails.ruleLevel.columns[0]);
          this.selectedAttributeName = this.fetchEditRule?.ruleDetails.ruleLevel.columns[0];
        }
        if(this.fetchEditRule?.ruleDetails.ruleLevel.levelName == 'Multiple Columns') {
          
          this.fg.controls.selectColumnAttribute.setValue(this.fetchEditRule?.ruleDetails.ruleLevel.columns);
          this.fetchEditRule?.ruleDetails.ruleLevel.columns.forEach(c => {
            this.completenessData.push(c); 
          })
          this.fetchEditRule?.ruleDetails.ruleLevel.columns.forEach(c => {
            this.selectedMultipleAttributeName.push(c); 
          })
        }

        this.fg.removeControl('dummyValidator');
        this.fg.updateValueAndValidity();
        this.loading = false;
        
        // this.fg.controls.selectColumnAttribute.setValue(this.fetchEditRule?.ruleDetails.ruleLevel.columns[0]);
        //   this.completenessData.push(this.fetchEditRule?.ruleDetails.ruleLevel.columns[0]);
        //   this.selectedAttributeName = this.fetchEditRule?.ruleDetails.ruleLevel.columns[0];
        // this.selectedAttributeName = this.fetchEditRule?.ruleDetails.ruleLevel.columns;
      }, 3000); // 5000 milliseconds (5 seconds)
      
      this.initializeCheckedDays();
    }, (error) => {

    })

    
  }

  initializeCheckedDays() {
    const selectedDaysArray = this.selectedDaysString.split(',');
    this.ruleDays.forEach(day => {
      day.isChecked = selectedDaysArray.includes(day.day);
    });
  }

  public getDataConnection(): void {
    this.ruleCreationService.getDataConnection(this.projectId, this.userId, this.connectionPage, this.connectionPageSize).subscribe((response) => {
      this.dataConnectionList = response?.content;
    }, (error) => {

    })
  }

  public getRuleTypeList(): void {
    this.ruleCreationService.getRuleTypes(this.projectId, !this.sourceTargetType).subscribe((response) => {
      this.ruleTypesData = response.result;
      
    }, (error) => {

    })
  }

  public getBucketData(): void {
    this.ruleCreationService.getBucketList(this.userId,this.projectId).subscribe((response) => {
      this.bucketList = response; 
    }, (error) => {

    });
  }

  public getRules(): void {
    // this.loading = true;
    this.ruleCreationService.getRulesData(this.userId, this.projectId, this.ruleStatus, this.pageNumebr, this.pageLength).subscribe((response) => {
      
      // this.loading = false;
      this.getRuleList = response?.result?.content;
      
    }, (error) => {
      // this.loading = false;
    });
  }

  public checkExistName(): void {
    const ruleName = this.fg.get('ruleName').value;
    const checkD = this.fetchEditRule?.ruleName.toLowerCase() === this.fg.get('ruleName').value.toLowerCase();
    if (!checkD){
      this.ruleCreationService.existRuleName(this.userId, this.projectId, ruleName, this.statusBody).subscribe((response) => {
      
        if (response.isExist) {
          this.alreadyExist = true;
        } else {
          this.alreadyExist = false;
        }
        
      }, (error) => {
  
      });
    }
    
  }

  public testUpdateData(): void {
    const ruleName = this.fg.get('ruleName').value;
    const checkD = this.fetchEditRule?.ruleName.toLowerCase() === this.fg.get('ruleName').value.toLowerCase();
    if (checkD){
      this.updateRuleFunction();
    } else {
      
      this.ruleCreationService.existRuleName(this.userId, this.projectId, ruleName, this.statusBody).subscribe((response) => {
      
        if (response.isExist) {
          this.alreadyExist = true;
          this.snakbar.open(response.message);
        } else {
          this.alreadyExist = false;
          this.updateRuleFunction();
        }
        
      }, (error) => {
  
      });
    }
  }

  checkIfNameExists() {
    this.alreadyExist = false;
    this.editableForm = true;
    if(this.fg.get('ruleName').value.length <= 0){
      this.alreadyExist = false;
    }
    
  }

  checkDescription(){
    this.editableForm = true;
  }

  public selectedType(type: boolean): void {
    // if (type) {
      
    // } else {

    // }
  }

  public selectedStatus(s: any): void {
    this.editableForm = true;
    // if (s == 'Deleted') {
    //   const modalRef = this.modalService.open(StatusDeleteConfirmationComponent, { size: 'md modal-dialog-centered', scrollable: false});
    //   modalRef.result.then((result) => {
    //     // this.fg.controls.status.setValue(s);
    //   }, (error) => {
    //     this.fg.controls.status.setValue(this.fetchEditRule?.status);
    //   });

    // } else {

    // }
  }

  public continue(): void {
    this.snakbar.open("Rule updated successfully!");
    this.router.navigate([`projects/${this.projectId}/data-quality/rule-types`]);
  }

  public showData(): void {
    this.isShowData = !this.isShowData;
    if (!this.fg.valid) {
      return;
    }

  }

  public back(): void {
    this.location.back();
  }

  public onChangeRuleTypes(value: string): void {
    this.selectedSubLevel = null;
    this.selectedMainRuleType = value;
    console.log("type:", value);
  }

  public onChangeSubLevel(value: string): void {
    this.selectedSubLevel = value;
  }

  // browse table 
  public browseTable(): void {
    this.router.navigate([`projects/${this.projectId}/data-quality/create/table`]);
  }

  modelChangeRuleName(str) {
    // const re = /^[a-zA-Z0-9_]+$/;

    if (this.fg.controls.ruleName.value != "") {

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

  modelChangeDescription(str) {
    if(this.fg.controls.ruleDescription.value != ""){
      const validPattern = String(str)
      .match(
        /^[A-Za-z0-9\s!@#$%^&*()\-_+=\[\]{}|;:'",.<>?/~`\\]*$/
      );

      if (validPattern == null) {
        this.invalidDesPattern = true;

      } else {
        this.invalidDesPattern = false;
      }
    } else {
      this.invalidDesPattern = false;
    }
  }

  validateInput(event: any): void {
    this.editableForm = true;
    const inputElement = event.target;
    const inputValue = inputElement.value;
  
    // Remove any non-numeric characters, including decimals
    const sanitizedValue = inputValue.replace(/[^0-9]/g, '');
  
    // Update the input value with the sanitized value
    inputElement.value = sanitizedValue;
    this.fg.controls.percentage.setValue(sanitizedValue);
    // debugger
  }

  public onSelectDataConnections1(d: string): void {
    const connectionBucket = this.dataConnectionList.find(e => e.id == +d);
    this.bucketSourceOne = []
    const arr = [];
    arr.push(connectionBucket.bucketName);
    this.bucketSourceOne.push(connectionBucket.bucketName);
  }

  public onSelectDataConnections2(d: string): void {
    const connectionBucket = this.dataConnectionList.find(e => e.id == +d);
    this.bucketSourceTwo = [];
    const arr = [];
    arr.push(connectionBucket.bucketName);
    this.bucketSourceTwo.push(connectionBucket.bucketName);
  }

  public onSelectBucketOne(d: string): void {

  }

  public onSelectBucketTwo(d: string): void {

  }

  public updateRuleFunction(): void {
    if (this.fg.controls.status.value == 'Deleted') {
      const modalRef = this.modalService.open(StatusDeleteConfirmationComponent, { size: 'md modal-dialog-centered', scrollable: false});
      modalRef.result.then((result) => {
        // this.fg.controls.status.setValue(s);
        this.updateData();
      }, (error) => {
        this.fg.controls.status.setValue(this.fetchEditRule?.status);
      });

    } else {
      this.updateData();
    }
    
    // this.router.navigate([`projects/${this.projectId}/data-quality`]);
  }

  checkedDays(event: any, day: string): void {
    this.editableForm = true;
    // Update the isChecked property of the corresponding day object
    const selectedDay = this.ruleDays.find(d => d.day === day);
    if (selectedDay) {
      selectedDay.isChecked = event.target.checked;
    }

    // Update the selectedDaysString based on the checked checkboxes
    this.selectedDaysString = this.getSelectedDaysString();
    console.log(this.selectedDaysString);
    this.fg.controls.ruleRunDays.setValue(this.selectedDaysString);
    
  }

  getSelectedDaysString(): string {
    // Get an array of selected days
    const selectedDays = this.ruleDays
      .filter(d => d.isChecked)
      .map(d => d.day);
  
    // Join the selected days into a comma-separated string
    return selectedDays.join(',');
  }

  // Function to check if any checkbox is selected
  anyCheckboxSelected(): boolean {
    return this.ruleDays.some(d => d.isChecked);
  }

  public updateData(): void {
    // const value = this.fg.get('ruleName').value;
    // const titleCaseValue = value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
    // this.fg.get('ruleName').setValue(titleCaseValue);
    if (this.fg.controls.sourceAndTarget.value) {
      this.saveRulePayload = {
        ruleId: this.ruleId,
        ruleName:this.fg.controls.ruleName.value,
        ruleDescription:this.fg.controls.ruleDescription.value,
        sourceAndTarget:this.fg.controls.sourceAndTarget.value,
        sourceData : {
            sourceDataType: this.fg.controls.sourceDataSource.value,
            subDataSourceType: this.fg.controls.subDataSourceOne.value,
            dataSourceId: +this.fg.controls.sourceDataConnection.value,
            bucketName: this.fg.controls.sourceBucketOne.value,
            region: this.fg.controls.region.value,
            filePath : this.fg.controls.sourceFolderPath.value,
            filePattern: this.fg.controls.sourcePatternPath.value
        },
        targetData: {
          sourceDataType: this.fg.controls.sourceDataSourceTwo.value,
          subDataSourceType: this.fg.controls.subDataSourceTwo.value,
          dataSourceId: +this.fg.controls.sourceDataConnectionTwo.value,
          bucketName: this.fg.controls.sourceBucketTwo.value,
          region: this.fg.controls.regionTwo.value,
          filePath : this.fg.controls.sourceFolderPathTwo.value,
          filePattern: this.fg.controls.sourcePatternPathTwo.value
        },
        ruleDetails:{
            ruleTypeName : this.fg.controls.ruleType.value,
            ruleLevel :{
              levelName: this.fg.controls.subLavelRadio.value,
              columnLevel: false,
              acceptance: this.fg.controls.percentage.value,
              columns: [this.fg.controls.selectColumnAttribute.value]
            }
    
        },
        userId : this.userId,
        status: this.fg.controls.status.value,
        ruleRunDays: this.selectedDaysString
      }
      // console.log("Rule save payload:", JSON.stringify(req));
    } else {
      if (this.fg.controls.subLavelRadio.value == 'Multiple Columns'){
        this.saveRulePayload = {
          ruleId: this.ruleId,
          ruleName:this.fg.controls.ruleName.value,
          ruleDescription:this.fg.controls.ruleDescription.value,
          sourceAndTarget:this.fg.controls.sourceAndTarget.value,
          sourceData : {
              sourceDataType: this.fg.controls.sourceDataSource.value,
              subDataSourceType: this.fg.controls.subDataSourceOne.value,
              dataSourceId: +this.fg.controls.sourceDataConnection.value,
              bucketName: this.fg.controls.sourceBucketOne.value,
              region: this.fg.controls.region.value,
              filePath : this.fg.controls.sourceFolderPath.value,
              filePattern: this.fg.controls.sourcePatternPath.value
          },
          ruleDetails:{
              ruleTypeName : this.fg.controls.ruleType.value,
              ruleLevel :{
                levelName: this.fg.controls.subLavelRadio.value,
                columnLevel: false,
                acceptance: this.fg.controls.percentage.value,
                columns: this.fg.controls.selectColumnAttribute.value
              }
      
          },
          userId : this.userId,
          status: this.fg.controls.status.value,
          ruleRunDays: this.selectedDaysString
        }
      } else {
        this.saveRulePayload = {
          ruleId: this.ruleId,
          ruleName:this.fg.controls.ruleName.value,
          ruleDescription:this.fg.controls.ruleDescription.value,
          sourceAndTarget:this.fg.controls.sourceAndTarget.value,
          sourceData : {
              sourceDataType: this.fg.controls.sourceDataSource.value,
              subDataSourceType: this.fg.controls.subDataSourceOne.value,
              dataSourceId: +this.fg.controls.sourceDataConnection.value,
              bucketName: this.fg.controls.sourceBucketOne.value,
              region: this.fg.controls.region.value,
              filePath : this.fg.controls.sourceFolderPath.value,
              filePattern: this.fg.controls.sourcePatternPath.value
          },
          ruleDetails:{
              ruleTypeName : this.fg.controls.ruleType.value,
              ruleLevel :{
                levelName: this.fg.controls.subLavelRadio.value,
                columnLevel: false,
                acceptance: this.fg.controls.percentage.value,
                columns: [this.fg.controls.selectColumnAttribute.value]
              }
      
          },
          userId : this.userId,
          status: this.fg.controls.status.value,
          ruleRunDays: this.selectedDaysString
        }
      }
      // console.log("FALSE -- Rule save payload:", JSON.stringify(req));
    }

    this.ruleCreationService.updateRule(this.userId, this.projectId, this.saveRulePayload).subscribe((response) => {
      if (response?.code === 200) {
        this.snakbar.open(response.message);
        this.router.navigate([`projects/${this.projectId}/data-quality`]);
      } else {
        this.snakbar.open(response.message);
      }
    }, (error) => {
      this.snakbar.open(error);
    });
  }
}
