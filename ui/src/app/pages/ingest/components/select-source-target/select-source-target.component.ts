import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Location } from '@angular/common';
import { AwsData } from '../../models/awsdata';
import { SourceDataService } from '../../services/source-data.service';

@Component({
  selector: 'app-select-source-target',
  templateUrl: './select-source-target.component.html',
  styleUrls: ['./select-source-target.component.scss']
})
export class SelectSourceTargetComponent {
  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject();
  public projectId: number;
  userId: number;

  public fg: FormGroup;
  public fgt: FormGroup;

  public sourceData: any;
  public loading: boolean;

  public targetData = [
    {id:1, name:'Bucket1'},
    {id:2, name:'Bucket2'},
    {id:3, name:'Bucket3'},
    {id:4, name:'Bucket4'},
    {id:5, name:'Bucket5'}
  ];
  public queryFolders: boolean;

  private awsData: AwsData;
  public connectionParams: any;

  constructor(
    private fb: FormBuilder,
    private snakbar: SnackbarService,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private router: Router,
    private location: Location,
    private sourceDataService: SourceDataService,
  ){
    this.awsData = JSON.parse(sessionStorage.getItem('awsData')) as AwsData;
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

    this.fg = this.fb.group({
      source: new FormControl('', Validators.required),
    });

    // this.fgt = this.fb.group({
    //   target: new FormControl('', Validators.required),
    // });
    
    console.log("d", this.awsData);
    this.getBucketList();
  }

  public getBucketList(): void {
    this.loading = true;
    this.sourceDataService.getBucketList(this.userId, this.projectId).subscribe((res) => {
      this.sourceData = res;
      this.loading = false;
    }, (error) =>{
      this.loading = false;
    })
  }

  public save(): void {
    console.log("bucketname:", this.fg.controls.source.value);

    this.connectionParams = {
      projectId: this.projectId,
      userId: this.userId,
      connectionName: this.awsData.connectionName,
      accessType: this.awsData.accessType,
      subDataSource: this.awsData.subDataSource,
      bucketName: this.fg.controls.source.value,
    } as AwsData;
    
    sessionStorage.setItem('awsData', JSON.stringify(this.connectionParams));
    // this.snakbar.open("Data source saved succesfully");
    // this.router.navigate([`projects/${this.projectId}/ingest/aws`]);
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
