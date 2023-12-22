import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import {  ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ViewResultComponent } from '../view-result/view-result.component';
import { RuleCreationService } from '../../services/rule-creation.service';
import { ConfirmationComponent } from '../confirmation/confirmation.component';

@Component({
  selector: 'app-view-running-rules',
  templateUrl: './view-running-rules.component.html',
  styleUrls: ['./view-running-rules.component.scss']
})
export class ViewRunningRulesComponent implements OnInit {
  // @Input() projectId: number;
  // @Input() userId: number;

  fg: FormGroup;
  projectId: number;
  userId: number;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject<void>();
  public loading:boolean;
  public runningList: any;
  public cancelBtn: boolean;
  public outputData: any;
  public startIndex: number = 0;
  public pageSize: number = 100;
  public endIndex: number = this.pageSize;
  public page = 1;

  anyCheckboxSelected: boolean = false;
  public ruleId: any = [];
  public selectedJobIds: number[] = [];
  public reRunStatus: boolean;
  public selectedReRunJobIds: number[] = [];
  selectedBusinessDate: string | null = null;
  public selectedRules = [];
  public statusBody: any = ["Not Started","Inprocess"];

  constructor(
    // public modal: NgbActiveModal,
    private snakbar: SnackbarService,
    private fb: FormBuilder,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private modalService: NgbModal,
    private ruleCreationService: RuleCreationService,
  ){
    // this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
        this.userId = +certificate.user_id;
      });
  }

  ngOnInit(): void {
    this.projectId = +localStorage.getItem('project_id');
    this.getRulJobs();
  }

  public getRulJobs(): void {
    // this.loading = true;
    const refreshPage = () => {
        this.autoRefresh();
    };
    this.ruleCreationService.runningRulesData(this.userId, this.projectId, this.statusBody).subscribe((response) => {
      console.log("Rule data:", response);
      if (response?.code != 500) {
        this.runningList = response?.result;
        this.loading = false;
        // Check if there is at least one object with jobStatus "Inprocess"
        const hasInprocessJob = this.runningList.some(item => item.jobStatus === 'Inprocess' || item.jobStatus === 'Not Started');
        this.reRunStatus = this.runningList.some(item => item.jobStatus === 'Failed' || item.jobStatus === 'Cancelled');
        

        this.runningList.forEach(view => {
          const jobId = view.jobId;
          if (view.jobStatus === 'Not Started' || view.jobStatus === 'Inprocess') {
            const isSelected = this.selectedJobIds.includes(jobId);
            view.isChecked = isSelected; // Add an 'isChecked' property to each item
          } else {
            // Check if jobId is in the selectedJobIds array
            const index = this.selectedJobIds.indexOf(jobId);
            if (index !== -1) {
              // If it is, remove it from the selectedJobIds array
              this.selectedJobIds.splice(index, 1);
            }
          }
          
        });

        if (hasInprocessJob) {
          setTimeout(refreshPage, 5000);
          this.cancelBtn = true;
        } else {
          this.cancelBtn = false;
        }
      } else {
        this.runningList = [];
        this.loading = false;

      }
      
    }, (error) => {
      this.loading = false;
    });
  }

  public autoRefresh(): void {
    // const autoRefreshInterval = 5000; // 5 seconds

    // const refreshPage = () => {
    //   location.reload();
    // };

    this.getRulJobs();
  }

  public viewResult(r: any): void {
    const data = JSON.parse(r.jobOutput);
    console.log("---", data);
    

    if (Array.isArray(data)) {
      // It's a JSON array, you can loop through its elements
      for (const obj of data) {
        // Process each object in the array
        this.outputData = obj
        // You can add your conditional logic here for JSON array items
      }
    } else {
      // It's a JSON object
      this.outputData = data;
      // You can add your conditional logic for JSON objects here
    }

    const modalRef = this.modalService.open(ViewResultComponent, { size: 'md', windowClass: 'custom-modal-class', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.output = this.outputData;
    modalRef.result.then((result) => {
      
    }, (result) => {
      
    });
  }

  public viewLog(v: any): void {
    localStorage.setItem('batchJobLog', v?.batchJobLog);
    this.router.navigate([]).then(() => { window.open(`/projects/${this.projectId}/data-quality/logs`, '_blank'); });
  }

  public cancelInprocess(): void {
    console.log(this.selectedJobIds);
    
    const modalRef = this.modalService.open(ConfirmationComponent, { size: 'md modal-dialog-centered', scrollable: false});
    modalRef.result.then((result) => {
      this.loading = true;
      // // Initialize an empty array to store jobIds
      // const jobIds: number[] = [];
  
      // // Iterate through the runningList to find objects with jobStatus "Inprocess"
      // for (const item of this.runningList) {
      //     if (item.jobStatus === 'Inprocess' || item.jobStatus === 'Not Started' ) {
      //         // Push the jobIds into the array
      //         jobIds.push(item.jobId);
      //     }
      // }
  
      const req = {
        jobIds : this.selectedJobIds
      };
  
      this.ruleCreationService.cancelRunningRule(this.userId, this.projectId, req).subscribe((res) => {
        this.loading = false;
        this.snakbar.open(res.message);
        this.getRulJobs();
      }, (error) => {
        this.loading = false;
      });
  
      // Now, 'jobIds' contains all the jobIds for objects with jobStatus "Inprocess"
      console.log(this.selectedJobIds); // You can use the 'jobIds' array as needed
    }, (error) => {
      this.snakbar.open(error);
    });
    
  }

  public reRunRuleFunction(): void {
    this.loading = true;
    const request = {
      rules: this.selectedRules
    }

    this.ruleCreationService.runRule(this.userId, this.projectId, request).subscribe((res) => {
      this.loading = false;
      this.selectedRules = [];
      this.selectedReRunJobIds = [];
      this.snakbar.open(res.message);
      this.getRulJobs();
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });
    
    // const modalRef = this.modalService.open(ConfirmationComponent, { size: 'md modal-dialog-centered', scrollable: false});
    // modalRef.result.then((result) => {
    //   this.loading = true;
  
    //   const req = {
    //     jobIds : this.selectedJobIds
    //   };
  
    //   this.ruleCreationService.cancelRunningRule(this.userId, this.projectId, req).subscribe((res) => {
    //     this.loading = false;
    //     this.snakbar.open(res.message);
    //     this.getRulJobs();
    //   }, (error) => {
    //     this.loading = false;
    //   });
  
    //   // Now, 'jobIds' contains all the jobIds for objects with jobStatus "Inprocess"
    //   console.log(this.selectedJobIds); // You can use the 'jobIds' array as needed
    // }, (error) => {
    //   this.snakbar.open(error);
    // });
    
  }

  public refresh(): void {
    // call get function
    this.getRulJobs();
  }

  public back(): void {
    this.router.navigate([`projects/${this.projectId}/data-quality`]);
  }

  // Define a function to parse the jobOutput JSON string
  parseJobOutput(jobOutput: string) {
    try {
      const parsedOutput = JSON.parse(jobOutput);
      if (Array.isArray(parsedOutput)) {
        // Check if it's an array and contains an item with 'match' property
        const hasMatch = parsedOutput.some(item => item.match);
        return hasMatch ? 'Match' : 'MisMatch';
      } else if (parsedOutput.match !== undefined) {
        // Check if it's an object with 'match' property
        return parsedOutput.match ? 'Match' : 'MisMatch';
      } else {
        // If it doesn't have 'match' property, default to 'MisMatch'
        return 'MisMatch';
      }
    } catch (error) {
      // Handle the case where JSON parsing fails
      return 'MisMatch';
    }
  }

  public onPageChange(currentPage: number): void {
    this.startIndex = (currentPage - 1) * this.pageSize;
    this.endIndex = this.startIndex + this.pageSize;
  }

  selectRule(event: any, d: any): void {
    const jobId = d.jobId;

    if (event.target.checked) {
      // Checkbox is checked, add jobId to the selectedJobIds array
      this.selectedJobIds.push(jobId);
    } else {
      // Checkbox is unchecked, remove jobId from the selectedJobIds array
      const index = this.selectedJobIds.indexOf(jobId);
      if (index !== -1) {
        this.selectedJobIds.splice(index, 1);
      }
    }
    
  }

  selectReRule(event: any, d: any): void {
    if (event.target.checked) {
      // If checkbox is checked, add the rule to the selectedRules array
      this.selectedRules.push({
        ruleId: d.ruleId,
        businessDate: d.businessDate
      });
    } else {
      // If checkbox is unchecked, remove the rule from the selectedRules array
      this.selectedRules = this.selectedRules.filter(selectedRule => selectedRule.ruleId !== d.ruleId);
    }

    // const jobId = d.ruleId;

    // // Clear the array before adding the new jobId
    // this.selectedReRunJobIds = [];

    // if (event.target.checked) {
    //   // Checkbox is checked, add jobId to the selectedJobIds array
    //   this.selectedReRunJobIds.push(jobId);

    //   // Find the object with the selected jobId
    //   const selectedJob = this.runningList.find(job => job.ruleId === jobId);

    //   // Extract the businessDate from the selected object
    //   if (selectedJob) {
    //     this.selectedBusinessDate = selectedJob.businessDate;
    //   } else {
    //     this.selectedBusinessDate = null;
    //   }
    // } else {
    //   // Checkbox is unchecked, reset the selectedBusinessDate
    //   this.selectedBusinessDate = null;
    // }
    
  }

  isSelected(jobId: number): boolean {
    // Check if the jobId is in the selectedReRunJobIds array
    return this.selectedReRunJobIds.includes(jobId);
  }

  public reRun(d: any): void {
    this.loading = true;
    this.selectedReRunJobIds = [];
    this.selectedReRunJobIds.push(d.ruleId);
    this.selectedBusinessDate = d.businessDate;

    
    const request = {
      ruleIds: this.selectedReRunJobIds,
      businessDate: this.selectedBusinessDate
    }

    this.ruleCreationService.runRule(this.userId, this.projectId, request).subscribe((res) => {
      this.loading = false;
      this.selectedReRunJobIds = [];
      this.snakbar.open(res.message);
      this.getRulJobs();
    }, (error) => {
      this.loading = false;
      this.selectedReRunJobIds = [];
      this.snakbar.open(error);
    });
  }

  convertToUKTimeZone(timestamp: number) {
    if(timestamp) {
      const ukTimestamp = new Date(timestamp);

      // Check if Daylight Saving Time (DST) is in effect (typically from the last Sunday in March to the last Sunday in October)
      const today = new Date();
      const dstStart = new Date(today.getFullYear(), 2, 31 - ((5 * today.getFullYear() / 4 + 4) % 7), 1); // Last Sunday in March
      const dstEnd = new Date(today.getFullYear(), 9, 31 - ((5 * today.getFullYear() / 4 + 1) % 7), 1); // Last Sunday in October
      const isDST = today > dstStart && today < dstEnd;

      // Adjust for DST (1 hour ahead if DST is in effect)
      if (isDST) {
        ukTimestamp.setHours(ukTimestamp.getHours() + 1);
      }

      const day = ukTimestamp.getUTCDate();
      const month = ukTimestamp.getUTCMonth() + 1; // Months are zero-based, so add 1
      const year = ukTimestamp.getUTCFullYear() % 100; // Get the last two digits of the year
      const hours = ukTimestamp.getUTCHours();
      const minutes = ukTimestamp.getUTCMinutes();

      // Format date components with leading zeros if needed
      const formattedDate = `${day < 10 ? '0' : ''}${day}-${month < 10 ? '0' : ''}${month}-${year}`;
      const formattedTime = `${hours < 10 ? '0' : ''}${hours}:${minutes < 10 ? '0' : ''}${minutes}`;
      const amPm = hours < 12 ? 'AM' : 'PM';

      return `${formattedDate}, ${formattedTime} ${amPm}`;
    }
    
  }
}
