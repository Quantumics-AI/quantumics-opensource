import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, ValidatorFn, FormArray } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Location } from '@angular/common';
import { BrowseFileComponent } from '../browse-file/browse-file.component';
import { RuleCreationService } from '../../services/rule-creation.service';
// import { ruleDays } from '../../models/rule-filter.model';

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
  selector: 'app-data-quality-create',
  templateUrl: './data-quality-create.component.html',
  styleUrls: ['./data-quality-create.component.scss']
})
export class DataQualityCreateComponent implements OnInit {
  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject();
  public fg: FormGroup;
  public sourceTargetType: boolean = true;
  public isShowData: boolean = false;
  public projectId: number;
  userId: number;
  public selectedNullName: string;
  public ruleTypesData: any;
  public selectedMainRuleType: string;
  public selectedSubLevel: string;
  invalidPattern: boolean = false;
  invalidCapitalPattern: boolean = false;
  invalidDesPattern: boolean = false;
  public dataConnectionList: any;
  public selectedDataConnections1: string;
  public selectedDataConnections2: string;
  public selectedAttributeName: any;
  public selectedMultipleAttributeName: any;
  public bucketList: any;
  public selectedBucketOne: string;
  public selectedBucketTwo: string;
  public connectionParams: any;
  public bucketSourceOne: any = [];
  public bucketSourceTwo: any = [];
  public saveRulePayload: any;
  public selectedSourcePathOne: string;
  public selectedSourcePathTwo: string;
  public formData: any;
  public selectedPath: string;
  public selectedFile: string;
  public selectedSource: string;
  public filecheck: boolean;
  public columnData: any;
  public columnDataType: any;
  public getRuleList: any;
  public loading: boolean
  public ruleStatus: string = 'Active';
  public pageNumebr: number = 1;
  public pageLength: number = 10000;
  public alreadyExist: boolean = false;
  public statusBody: any = ["Active","Inactive"];
  public selectedSubColumn: string;
  public patternPath: string;
  public isPattern: boolean;
  public selectedDaysString: string = "Mon,Tue,Wed,Thu,Fri";
  public selectedDays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
  public ruleDays = [
    {id: 1, day: 'Mon', label: 'Mon', isChecked: true},
    {id: 2, day: 'Tue', label: 'Tue', isChecked: true},
    {id: 3, day: 'Wed', label: 'Wed', isChecked: true},
    {id: 4, day: 'Thu', label: 'Thu', isChecked: true},
    {id: 5, day: 'Fri', label: 'Fri', isChecked: true},
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
    private cdr: ChangeDetectorRef) {
      this.formData = JSON.parse(sessionStorage.getItem('formData'));
      this.selectedPath = sessionStorage.getItem('SelectedPath');
      this.patternPath = sessionStorage.getItem('selectedPattern');
      this.selectedFile = sessionStorage.getItem('selectedFile');
      this.selectedSource = sessionStorage.getItem('source');
      this.filecheck = JSON.parse(sessionStorage.getItem('check'));
      this.columnData = JSON.parse(sessionStorage?.getItem('headersData'));
      this.columnDataType = JSON.parse(sessionStorage?.getItem('columnData'));
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
    const ruleDescriptionPattern = /^[A-Za-z0-9\s!@#$%^&*()\-_+=\[\]{}|;:'",.<>?/~`\\]*$/;
    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');
    this.fg = this.fb.group({ 
      // '^[A-Za-z\\s!@#$%^&*()\\-_+=\\[\\]{}|;:\'",.<>?/]*$' ---- Validators.minLength(10), Validators.maxLength(150),
      ruleName: ['', [Validators.required, Validators.pattern(/^[A-Z]([A-Za-z0-9_:-]+\s)*[A-Za-z0-9_:-]*$/), Validators.minLength(3)]],
      ruleDescription: ['', [Validators.required, Validators.pattern(ruleDescriptionPattern)]],
      sourceAndTarget: [true],
      //source
      //source1 - source2
      sourceDataSource: new FormControl('aws', Validators.required),
      subDataSourceOne: new FormControl('s3', Validators.required),
      sourceDataConnection: new FormControl('', Validators.required),
      sourceBucketOne: new FormControl('', Validators.required),
      region: new FormControl (''),
      accessType: new FormControl(''),
      sourceFolderPath: new FormControl('', Validators.required),
      sourcePatternPath: new FormControl('s3://BUCKET_NAME/FEED_NAME/YYYYMMDD/FILENAME', Validators.required),
      //
      sourceDataSourceTwo: new FormControl('aws', Validators.required),
      subDataSourceTwo: new FormControl('s3', Validators.required),
      sourceDataConnectionTwo: new FormControl('', Validators.required),
      sourceBucketTwo: new FormControl('', Validators.required),
      regionTwo: new FormControl (''),
      accessTypeTwo: new FormControl(''),
      sourceFolderPathTwo: new FormControl('', Validators.required),
      sourcePatternPathTwo: new FormControl('s3://BUCKET_NAME/FEED_NAME/YYYYMMDD/FILENAME', Validators.required),
      //types
      ruleType: new FormControl('', Validators.required),
      subLavelRadio: new FormControl('', Validators.required),
      selectColumnAttribute: [''],
      selectMultipleAttribute: new FormControl(''),
      acceptancePercentage: new FormControl(0, [Validators.pattern('^[0-9]*$'),acceptancePercentageValidator,]),
      //Rule configuration
      ruleRunDays: new FormControl(this.selectedDaysString, Validators.required)
    });
    if(this.columnDataType){
      this.columnDataType = this.removeDuplicates(this.columnDataType, "columnName");
      // console.log("removed duplicate column", uniqueData);
    }

    this.getDataConnection();
    this.getBucketData();
    // this.getRules();
  }

  public removeDuplicates(array: any[], key: string) {
    return array.filter((obj, index, self) =>
      index === self.findIndex((o) => o[key] === obj[key])
    );
  }

  ngAfterViewInit(): void {
    if (this.formData) {
      this.fg.controls.ruleName.setValue(this.formData.ruleName);
      this.fg.controls.ruleDescription.setValue(this.formData.ruleDescription);
      this.fg.controls.sourceAndTarget.setValue(this.formData?.sourceAndTarget);
      if(!this.formData?.sourceAndTarget){
        this.fg.get('sourceDataSourceTwo')?.clearValidators();
        this.fg.get('subDataSourceTwo')?.clearValidators();
        this.fg.get('sourceDataConnectionTwo')?.clearValidators();
        this.fg.get('sourceBucketTwo')?.clearValidators();
        this.fg.get('sourceFolderPathTwo')?.clearValidators();
        this.fg.get('sourcePatternPathTwo')?.clearValidators();

        this.fg.get('sourceDataSourceTwo')?.updateValueAndValidity();
        this.fg.get('subDataSourceTwo')?.updateValueAndValidity();
        this.fg.get('sourceDataConnectionTwo')?.updateValueAndValidity();
        this.fg.get('sourceBucketTwo')?.updateValueAndValidity();
        this.fg.get('sourceFolderPathTwo')?.updateValueAndValidity();
        this.fg.get('sourcePatternPathTwo')?.updateValueAndValidity();
      }
      this.fg.controls.sourceDataConnection.setValue(this.formData?.sourceDataConnection);
      this.fg.controls.sourceBucketOne.setValue(this.formData?.sourceBucketOne);
      this.bucketSourceOne.push(this.formData?.sourceBucketOne);
      this.fg.controls.region.setValue(this.formData?.region);
      this.fg.controls.accessType.setValue(this.formData?.accessType);
      if (this.filecheck) {
        if (this.selectedSource == 'source-1') {
          this.fg.controls.sourceFolderPath.setValue(this.selectedPath);
          this.fg.controls.sourceFolderPathTwo.setValue(this.formData?.sourceFolderPathTwo);
          // this.fg.controls.sourcePatternPath.setValue(this.patternPath);
          // this.fg.controls.sourcePatternPathTwo.setValue(this.formData?.sourcePatternPathTwo);
  
        } else {
          this.fg.controls.sourceFolderPath.setValue(this.formData?.sourceFolderPath);
          this.fg.controls.sourceFolderPathTwo.setValue(this.selectedPath);
          // this.fg.controls.sourcePatternPathTwo.setValue(this.patternPath);
          // this.fg.controls.sourcePatternPath.setValue(this.formData?.sourcePatternPath);
        }
      } else {
        this.fg.controls.sourceFolderPath.setValue(this.formData?.sourceFolderPath);
        this.fg.controls.sourceFolderPathTwo.setValue(this.formData?.sourceFolderPathTwo);
      }
      this.fg.controls.sourceDataConnectionTwo.setValue(this.formData?.sourceDataConnectionTwo);
      this.fg.controls.sourceBucketTwo.setValue(this.formData?.sourceBucketTwo);
      this.bucketSourceTwo.push(this.formData?.sourceBucketTwo);
      this.fg.controls.regionTwo.setValue(this.formData?.regionTwo);
      this.fg.controls.accessTypeTwo.setValue(this.formData?.accessTypeTwo);
      this.checkExistName();
    } else {
    }
    this.getRuleTypeList();
  }

  // public getRules(): void {
  //   this.loading = true;
  //   this.ruleCreationService.getRulesData(this.userId, this.projectId, this.ruleStatus, this.pageNumebr, this.pageLength).subscribe((response) => {
      
  //     this.loading = false;
  //     this.getRuleList = response?.result?.content;
      
  //   }, (error) => {
  //     this.loading = false;
  //   });
  // }

  public checkExistName(): void {
    const ruleName = this.fg.get('ruleName').value;
    this.ruleCreationService.existRuleName(this.userId, this.projectId, ruleName, this.statusBody).subscribe((response) => {
      
      if (response.isExist) {
        this.alreadyExist = true;
      } else {
        this.alreadyExist = false;
      }
      
    }, (error) => {

    });
  }

  checkIfNameExists() {
    this.alreadyExist = false;
    if(this.fg.get('ruleName').value.length <= 0){
      this.alreadyExist = false;
    }
    // console.log("+++++");
    
    // if (this.getRuleList.length > 0) {
    //   const value = this.fg.get('ruleName').value;
    //   const titleCaseValue = value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();

    //   // const enteredName = this.fg.get('dataSourceName').value;
    //   const nameExists = this.getRuleList.some(item => item.ruleName === titleCaseValue);
      
    //   if (nameExists) {
    //     this.alreadyExist = true;
    //   } else {
    //     this.alreadyExist = false;
    //   }
    // }
    
  }

  validateInput(event: any): void {
    const inputElement = event.target;
    const inputValue = inputElement.value;
  
    // Remove any non-numeric characters, including decimals
    const sanitizedValue = inputValue.replace(/[^0-9]/g, '');
  
    // Update the input value with the sanitized value
    inputElement.value = sanitizedValue;
    this.fg.controls.acceptancePercentage.setValue(sanitizedValue);
    // debugger
  }

  // check max 5 column 
  validateMaxSelection(maxSelection: number): ValidatorFn {
    return (control) => {
      const selectedOptions = control.value;
      if (selectedOptions && selectedOptions.length > maxSelection) {
        return { maxSelectionExceeded: true };
      }
      return null;
    };
  }

  // public loadMoreData(): void {
  //   // Increase the page number
  //   this.pageLength += 5;
  
  //   // Call the endpoint to fetch the next set of data
  //   this.getDataConnection();
  // }

  public getDataConnection(): void {
    this.ruleCreationService.getDataConnection(this.projectId, this.userId, this.pageNumebr, this.pageLength).subscribe((response) => {
      this.dataConnectionList = response?.content;
    }, (error) => {

    })
  }

  // public onSelectScroll(event: Event): void {
  //   // Check if the user has scrolled to the bottom
  //   const target = event.target as HTMLSelectElement;
  //   if (target.scrollTop + target.clientHeight >= target.scrollHeight) {
  //     this.loadMoreData();
  //   }
  // }

  public getRuleTypeList(): void {
    const v = this.fg.get('sourceAndTarget').value;
    this.ruleCreationService.getRuleTypes(this.projectId, !v).subscribe((response) => {
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


  // change radio button 
  public selectedType(type: boolean): void {
    this.getRuleTypeList();
    console.log("...", type);

    if (type) {
      this.fg.get('sourceDataSourceTwo').setValidators([Validators.required]);
      this.fg.get('subDataSourceTwo').setValidators([Validators.required]);
      this.fg.get('sourceDataConnectionTwo').setValidators([Validators.required]);
      this.fg.get('sourceBucketTwo').setValidators([Validators.required]);
      this.fg.get('sourceFolderPathTwo').setValidators([Validators.required]);
      this.fg.get('sourcePatternPathTwo').setValidators([Validators.required]);
    } else {
      this.fg.get('sourceDataSourceTwo')?.clearValidators();
      this.fg.get('subDataSourceTwo')?.clearValidators();
      this.fg.get('sourceDataConnectionTwo')?.clearValidators();
      this.fg.get('sourceBucketTwo')?.clearValidators();
      this.fg.get('sourceFolderPathTwo')?.clearValidators();
      this.fg.get('sourcePatternPathTwo')?.clearValidators();
    }
    
    this.fg.get('sourceDataSourceTwo')?.updateValueAndValidity();
    this.fg.get('subDataSourceTwo')?.updateValueAndValidity();
    this.fg.get('sourceDataConnectionTwo')?.updateValueAndValidity();
    this.fg.get('sourceBucketTwo')?.updateValueAndValidity();
    this.fg.get('sourceFolderPathTwo')?.updateValueAndValidity();
    this.fg.get('sourcePatternPathTwo')?.updateValueAndValidity();

    // sourceDataSource: new FormControl('aws', Validators.required),
    //   subDataSourceOne: new FormControl('s3', Validators.required),
    //   sourceDataConnection: new FormControl('', Validators.required),
    //   sourceBucketOne: new FormControl('', Validators.required),
    //   sourceFolderPath: new FormControl({ value: '', disabled: true }, Validators.required),
    //   //
    //   sourceDataSourceTwo: new FormControl('aws', Validators.required),
    //   subDataSourceTwo: new FormControl('s3', Validators.required),
    //   sourceDataConnectionTwo: new FormControl('', Validators.required),
    //   sourceBucketTwo: new FormControl('', Validators.required),
    //   sourceFolderPathTwo: new FormControl({ value: '', disabled: true }, Validators.required),



    // if (type === 'sourceAndTarget') {
    //   this.fg.get('sourceDataSource').setValidators([Validators.required]);
    //   this.fg.get('sourceDataConnection').setValidators([Validators.required]);
    //   this.fg.get('sourceBucket').setValidators([Validators.required]);
    //   this.fg.get('sourceFolder').setValidators([Validators.required]);
    // } else {
    //   this.fg.get('sourceDataSource')?.clearValidators();
    //   this.fg.get('sourceDataConnection')?.clearValidators();
    //   this.fg.get('sourceBucket')?.clearValidators();
    //   this.fg.get('sourceFolder')?.clearValidators();
    // }

    // this.fg.get('sourceDataSource')?.updateValueAndValidity();
    // this.fg.get('sourceDataConnection')?.updateValueAndValidity();
    // this.fg.get('sourceBucket')?.updateValueAndValidity();
    // this.fg.get('sourceFolder')?.updateValueAndValidity();
    if (type) {
      
    } else {

    }
  }

  p

  public continue(): void {
    // this.router.navigate([`projects/${this.projectId}/data-quality/rule-types`]);
    this.snakbar.open("Rule saved");
    this.router.navigate([`projects/${this.projectId}/data-quality`]);
  }

  public showData(): void {
    this.isShowData = !this.isShowData;
    if (!this.fg.valid) {
      return;
    }

  }

  public back(): void {
    this.router.navigate([`projects/${this.projectId}/data-quality`]);
  }

  public onChangeRuleTypes(value: string): void {
    this.fg.get('subLavelRadio').setValidators([Validators.required]);
    this.fg.get('subLavelRadio')?.updateValueAndValidity();
    this.fg.controls.selectColumnAttribute.setValue('');
    this.fg.controls.acceptancePercentage.setValue(0);
    // this.fg.get('selectColumnAttribute')?.clearValidators();
    // this.fg.get('selectColumnAttribute')?.updateValueAndValidity();
    this.selectedSubLevel = null;
    this.selectedMainRuleType = value;

    if(value === 'Null Value'){
      this.fg.get('subLavelRadio')?.clearValidators();
      this.fg.get('selectColumnAttribute').setValidators([Validators.required]);
      // this.fg.get('acceptancePercentage').setValidators([Validators.required]);
      
    } else {
      this.fg.get('subLavelRadio').setValidators([Validators.required]);
      // this.fg.get('acceptancePercentage')?.clearValidators();
    }

    if (value === 'Zero Row Check') {
      this.fg.get('subLavelRadio')?.clearValidators();
      this.fg.get('selectColumnAttribute')?.clearValidators();
    }

    this.fg.get('subLavelRadio')?.updateValueAndValidity();
    this.fg.get('selectColumnAttribute')?.updateValueAndValidity();
    this.fg.get('acceptancePercentage')?.updateValueAndValidity();
    // subLavelRadio
    // this.fg.get('sourceFolderPathTwo').setValidators([Validators.required]);
    // this.fg.get('selectColumnAttribute')?.clearValidators();
    // this.fg.get('selectColumnAttribute')?.updateValueAndValidity();
    
  }

  public onChangeSubLevel(value: string): void {
    this.fg.get('selectColumnAttribute')?.clearValidators();
    this.fg.controls.selectColumnAttribute.setValue('');
    this.fg.controls.acceptancePercentage.setValue(0);
    this.selectedSubLevel = value;
    console.log("sub level -", value);

    if (value == 'Rows') {
      this.fg.get('selectColumnAttribute')?.clearValidators();
    }

    if (value == 'Single Column') {
      console.log("...", this.selectedMainRuleType);
      this.selectedSubColumn = "in the Duplicate - Single Column Value Rule Type";
      
      this.fg.get('selectColumnAttribute').setValidators([Validators.required]);
    }
    
    if (value == "Multiple Columns"){
      // this.fg.get('selectColumnAttribute').setValidators([Validators.required]);
      this.fg.get('selectColumnAttribute').setValidators([Validators.required, Validators.minLength(2), Validators.maxLength(5)]);
    }

    if (value == 'Row count check') {
      this.fg.get('selectColumnAttribute')?.clearValidators();
    }

    if (value == 'Sum of column value') {
      this.selectedSubColumn = "under Data Completeness Rule";
      this.fg.get('selectColumnAttribute').setValidators([Validators.required]);
    }

    if (value == 'Table level') {
      this.fg.get('selectColumnAttribute')?.clearValidators();
    }

    if (value == 'Column level') {
      this.selectedSubColumn = "in the Data Profiler Rule at Column Level";
      this.fg.get('selectColumnAttribute').setValidators([Validators.required]);
    }

    this.fg.get('selectColumnAttribute')?.updateValueAndValidity();

    if (value == "Sum of column value" || value === "Column level") {
      if (this.columnDataType) {
        this.columnDataType = this.columnDataType.filter(item => item.dataType === "int" || item.dataType === "float" || item.dataType === "double");
      } 
    } else {
      this.columnDataType = JSON.parse(sessionStorage?.getItem('columnData'));
    }
    
  }

  checkedDays(event: any, day: string): void {
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

  // browse table 
  public browseTable(): void {
    this.router.navigate([`projects/${this.projectId}/data-quality/create/table`]);
  }

  public browseSourceOne(s: string): void {
    sessionStorage.setItem("source", s);
    sessionStorage.setItem("bucketName", this.fg.get('sourceBucketOne').value);
    sessionStorage.setItem("region", this.fg.get('region').value);
    sessionStorage.setItem("accessType", this.fg.get('accessType').value);
    const form_obj = {};
    Object.keys(this.fg.controls).forEach(key => {
      form_obj[key] = this.fg.get(key).value;
    });
    sessionStorage.setItem("formData", JSON.stringify(form_obj));
    sessionStorage.setItem('check', 'false');
    this.router.navigate([`projects/${this.projectId}/data-quality/create/table`]);
  }

  public browseSourceTwo(s: string): void {
    sessionStorage.setItem("source", s);
    sessionStorage.setItem("bucketName", this.fg.get('sourceBucketTwo').value);
    sessionStorage.setItem("region", this.fg.get('regionTwo').value);
    sessionStorage.setItem("accessType", this.fg.get('accessTypeTwo').value);
    const form_obj = {};
    Object.keys(this.fg.controls).forEach(key => {
      form_obj[key] = this.fg.get(key).value;
    });
    sessionStorage.setItem("formData", JSON.stringify(form_obj));
    sessionStorage.setItem('check', 'false');
    this.router.navigate([`projects/${this.projectId}/data-quality/create/table`]);
  }

  // public browseFileSourceOne(s: string): void {
  //   sessionStorage.setItem()
  //   this.router.navigate([`projects/${this.projectId}/data-quality/create/table`]);
  // }

  modelChangeRuleName(str) {
    const ruleNameControl = this.fg.controls.ruleName;

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

  public onSelectDataConnections1(id: string): void {
    this.bucketSourceOne = [];
    this.columnData = [];
    this.columnDataType = [];
    // this.fg.controls.sourceBucketOne.setValue('');
    this.fg.controls.sourceFolderPath.setValue('');
    const connectionBucket = this.dataConnectionList.find(e => e.id == +id);
    this.bucketSourceOne.push(connectionBucket.bucketName);
    this.fg.controls.sourceBucketOne.setValue(connectionBucket.bucketName);
    this.fg.controls.region.setValue(connectionBucket.region);
    this.fg.controls.accessType.setValue(connectionBucket.accessType);
  }

  public onSelectDataConnections2(id: string): void {
    this.bucketSourceTwo = [];
    // this.fg.controls.sourceBucketTwo.setValue('');
    this.fg.controls.sourceFolderPathTwo.setValue('');
    
    const connectionBucket = this.dataConnectionList.find(e => e.id == id);
    this.bucketSourceTwo.push(connectionBucket.bucketName);
    this.fg.controls.sourceBucketTwo.setValue(connectionBucket.bucketName);
    this.fg.controls.regionTwo.setValue(connectionBucket.region);
    this.fg.controls.accessTypeTwo.setValue(connectionBucket.accessType);
  }

  public onSelectBucketOne(d: string): void {

  }

  public onSelectBucketTwo(d: string): void {

  }

  // onInput(event: any) {
  //   // Get the input element
  //   const inputElement = event.target;
    
  //   // Remove any dots (.) from the input value
  //   inputElement.value = inputElement.value.replace(/\./g, '');
  // }

  public saveRuleFunction(): void {
    // const value = this.fg.get('ruleName').value;
    // const titleCaseValue = value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
    // this.fg.get('ruleName').setValue(titleCaseValue);
    if (this.fg.controls.sourceAndTarget.value) {
      this.saveRulePayload = {
        ruleName:this.fg.controls.ruleName.value,
        ruleDescription:this.fg.controls.ruleDescription.value,
        sourceAndTarget:this.fg.controls.sourceAndTarget.value,
        sourceData : {
            dataSourceType: this.fg.controls.sourceDataSource.value,
            subDataSourceType: this.fg.controls.subDataSourceOne.value,
            dataSourceId: +this.fg.controls.sourceDataConnection.value,
            bucketName: this.fg.controls.sourceBucketOne.value,
            region: this.fg.controls.region.value,
            filePath : this.fg.controls.sourceFolderPath.value,
            filePattern: this.fg.controls.sourcePatternPath.value
        },
        targetData: {
          dataSourceType: this.fg.controls.sourceDataSourceTwo.value,
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
              acceptance: this.fg.controls.acceptancePercentage.value,
              columns: [this.fg.controls.selectColumnAttribute.value]
            }
    
        },
        userId : this.userId,
        ruleRunDays: this.selectedDaysString
      }
      // console.log("Rule save payload:", JSON.stringify(req));
    } else {
      if (this.fg.controls.subLavelRadio.value == 'Multiple Columns') {
        this.saveRulePayload = {
          ruleName:this.fg.controls.ruleName.value,
          ruleDescription:this.fg.controls.ruleDescription.value,
          sourceAndTarget:this.fg.controls.sourceAndTarget.value,
          sourceData : {
              dataSourceType: this.fg.controls.sourceDataSource.value,
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
                acceptance: this.fg.controls.acceptancePercentage.value,
                columns: this.fg.controls.selectColumnAttribute.value
              }
      
          },
          userId : this.userId,
          ruleRunDays: this.selectedDaysString  
        }
      } else {
        this.saveRulePayload = {
          ruleName:this.fg.controls.ruleName.value,
          ruleDescription:this.fg.controls.ruleDescription.value,
          sourceAndTarget:this.fg.controls.sourceAndTarget.value,
          sourceData : {
              dataSourceType: this.fg.controls.sourceDataSource.value,
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
                acceptance: this.fg.controls.acceptancePercentage.value,
                columns: [this.fg.controls.selectColumnAttribute.value]
              }
      
          },
          userId : this.userId,
          ruleRunDays: this.selectedDaysString 
        }
      }
      // console.log("FALSE -- Rule save payload:", JSON.stringify(req));
    }

    this.ruleCreationService.saveRule(this.userId, this.projectId, this.saveRulePayload).subscribe((response) => {
      if(response.code == 400){
        this.snakbar.open(response.message);
        if (response.message == "Please check, the selected file is not align with the File Pattern") {
          this.isPattern = true;
        }
      } else {
        this.snakbar.open(response.message);
        this.router.navigate([`projects/${this.projectId}/data-quality`]);
      }
      
    }, (error) => {
      this.snakbar.open(error);
    });
  }

}