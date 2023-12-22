import { Component, OnInit, EventEmitter, Output, Input } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { SourceDataService } from '../../services/source-data.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { AwsConfirmationComponent } from '../aws-confirmation/aws-confirmation.component';
import { EditDataSourceComponent } from '../edit-data-source/edit-data-source.component';

@Component({
  selector: 'app-list-aws',
  templateUrl: './list-aws.component.html',
  styleUrls: ['./list-aws.component.scss']
})
export class ListAwsComponent {

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  private certificateData: Certificate;

  projectId: string;
  userId: number;
  loading: boolean;

  searchTerm: any = { connectionName: '' };
  public searchDiv: boolean = false;
  public searchString: string;
  public startIndex: number = 0;
  public pageSize: number = 10;
  public endIndex: number = this.pageSize;
  public page = 1;
  // public awsListData = [
  //   {id:1, name : "AWS1", access_type: "IAM", role: "user", iam_role: "Naveen", createdDate: new Date(), modifiedDate: new Date() },
  //   {id:2, name : "AWS2", access_type: "IAM", role: "user", iam_role: "Naveen", createdDate: new Date(), modifiedDate: new Date() },
  //   {id:3, name : "AWS3", access_type: "IAM", role: "user", iam_role: "Naveen", createdDate: new Date(), modifiedDate: new Date() },
  //   {id:4, name : "AWS4", access_type: "IAM", role: "user", iam_role: "Naveen", createdDate: new Date(), modifiedDate: new Date() },
  //   {id:5, name : "AWS5", access_type: "IAM", role: "user", iam_role: "Naveen", createdDate: new Date(), modifiedDate: new Date() }
  // ];

  public sourceListData: any;
  public isDescending: boolean;
  public timezone: any;

  public connectionCount: any;
  public pageNumebr: number = 1;
  public pageLength: number = 100;
  buttonDisabled: boolean = true;
  searchSuccessClass: string = 'search-success-btn';
  searchInvalidClass: string = 'search-disable-btn';
  public searchNull: boolean = false;
  public filter: boolean = true;
  public paginationData: any;
  public isSearch: boolean = false;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private sourceDataService: SourceDataService,
    private snakbar: SnackbarService
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
  }

  ngOnInit(): void {
    this.projectId = localStorage.getItem('project_id');
    this.getAwsList();
    sessionStorage.clear();
  }

  public getAwsList(): void {
    // this.loading = true;
    this.sourceDataService.getSourceData(+this.projectId, this.userId, this.pageNumebr, this.pageLength).subscribe((response) => {
      this.loading = false;
      console.log("getAws", response);
      this.sourceListData = response?.content;
      this.connectionCount = response?.content;
      this.paginationData = response;
      // if (this.sourceListData.length > 1) {
      //   this.sourceListData.sort((val1, val2) => {
      //     return (
      //       (new Date(val2.createdDate) as any) -
      //       (new Date(val1.createdDate) as any)
      //     );
      //   });
      // }
    }, (error) => {
      this.loading = false;
    })
  }

  public searchData(): void {
    this.isSearch = true;
    this.pageNumebr = 1;
    this.searchRule();
  }

  public searchRule(): void {
    this.buttonDisabled = true;
    this.sourceDataService.getSearchConnection(this.userId, +this.projectId, this.searchString, this.filter, this.pageNumebr, this.pageLength).subscribe((response) => {
      // debugger
      console.log("------->",response);
      if (response.code === 400) {
        this.searchNull = true;
        this.paginationData = [];
      } 
      if(response.code === 200){
        this.searchNull = false;
        this.sourceListData = response?.result?.content;
        this.connectionCount = response?.result?.content;
        this.paginationData = response?.result;
      }
      
    }, (error) => {
      
    });
  }


  parseConnectionData(connectionData: string): { iam: string } | null {
    try {
        const parsedData = JSON.parse(connectionData);
        return parsedData;
    } catch (error) {
        return null; // Handle parsing errors if needed
    }
  }

  // Sort data 
  public sortSorceData(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.sourceListData = this.sourceListData.sort((a, b) => {
        var source_name_order = a.connectionName.localeCompare(b.connectionName);
        return source_name_order;
      });
    } else {
      this.sourceListData = this.sourceListData.sort((a, b) => {
        var source_name_order = b.connectionName.localeCompare(a.connectionName);
        return source_name_order;
      });
    }

  }

  public sortAccessType(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.sourceListData = this.sourceListData.sort((a, b) => {
        var access = a.accessType.localeCompare(b.accessType);
        return access;
      });
    } else {
      this.sourceListData = this.sourceListData.sort((a, b) => {
        var access = b.accessType.localeCompare(a.accessType);
        return access;
      });
    }
  }

  sortDataByCreatedDate() {
    this.isDescending = !this.isDescending;
    if (this.isDescending) {
      this.sourceListData.sort((a, b) => a.createdDate - b.createdDate);
    } else {
      this.sourceListData.sort((a, b) => b.createdDate - a.createdDate);
    }
    
  }

  sortDataByModifiedDate(){
    this.isDescending = !this.isDescending;
    if (this.isDescending) {
      this.sourceListData.sort((a, b) => a.modifiedDate - b.modifiedDate);
    } else {
      this.sourceListData.sort((a, b) => b.modifiedDate - a.modifiedDate);
    }
  }

  updatedSourceData(data: any): void {
    const index = this.sourceListData.findIndex(x => x.id === data?.id);
    this.sourceListData[index] = data;
  }

  public edit(d: any): void {
    // console.log(d);
    // const connectionData = JSON.parse(d.connectionData);
    const modalRef = this.modalService.open(EditDataSourceComponent, { size: 'md', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.awsId = d.id;
    modalRef.componentInstance.sourceData = d;
    // modalRef.componentInstance.rolename = connectionData.iam;

    modalRef.result.then((result) => {
      this.updatedSourceData(result);
      this.getAwsList();
    }, (result) => {
      
     });
  }

  public delete(id:number): void {
    const modalRef = this.modalService.open(AwsConfirmationComponent, { size: 'md modal-dialog-centered', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.awsId = id;

    modalRef.result.then((result) => {
      this.loading = true;
      this.sourceDataService.deleteSourceData(+this.projectId, this.userId, id).subscribe((res:any) => {
        this.loading = false;
        this.snakbar.open("Data source deleted successfully!");
        const idx = this.sourceListData.findIndex(x => x.id === id);
        this.sourceListData.splice(idx, 1);
        this.getAwsList();
      }, (error) => {
        this.loading = false;
        this.snakbar.open(error);
      });
      
    }, (result) => {
      
     });
  }

  searchInput(str) {
    this.searchString = str;
    this.buttonDisabled = str.trim() === '';
    this.searchNull = false;
    this.getAwsList();
    if (str.length == 0) {
      this.searchDiv = false;
      this.isSearch = false;
      this.getAwsList();
    } else {
      this.searchDiv = true;
    }
  }

  clearSearhInput() {
    this.searchNull = false;
    this.searchTerm = { connectionName: '' };
    this.searchDiv = false;
    this.buttonDisabled = true;
    this.isSearch = false;
    this.getAwsList();
  }

  public onPageChange(currentPage: number): void {
    if(this.pageNumebr < currentPage){
      this.pageNumebr = currentPage;
    } else {
      this.pageNumebr = currentPage;
    }
    if(this.isSearch){
      this.searchRule();
    } else {
      this.getAwsList();
    }
    
    // this.startIndex = (currentPage - 1) * this.pageSize;
    // this.endIndex = this.startIndex + this.pageSize;
  }

  convertToUKTime(createdDate: number): string {
    // Convert createdDate from IST to UK time
    const date = new Date(createdDate);
    
    const ukTimeZone = 'Europe/London'; // Timezone for the United Kingdom
    
    // Format the date in the desired format
    this.timezone = {
      timeZone: ukTimeZone,
      year: '2-digit',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    };

    // Use a custom format for the day without leading zero
    const ukDate = new Intl.DateTimeFormat('en-GB', this.timezone).format(date);
    const parts = ukDate.split('/');
    const formattedDate = parts.map((part, index) => (index === 0 ? part.slice(1) : part)).join('-');

    return formattedDate;
  }

  convertToUKTimeZone(timestamp: number) {
    if(timestamp){
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
