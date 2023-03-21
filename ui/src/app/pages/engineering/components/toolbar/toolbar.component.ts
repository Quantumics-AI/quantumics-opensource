import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EngineeringService } from '../../services/engineering.service';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, Observable, Subject, Subscription, timer } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { takeUntil } from 'rxjs/operators';
import { Quantumfacade } from 'src/app/state/quantum.facade';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent {
  @Input() isPreview: boolean;
  @Output() addJoinPlusSign = new EventEmitter<void>();
  @Output() addTotalElement = new EventEmitter<void>();
  @Output() addJoinElement = new EventEmitter<void>();
  @Output() deleteElement = new EventEmitter<void>();
  @Output() udf = new EventEmitter<void>();
  @Output() pgsql = new EventEmitter<void>();
  @Output() addUdfElement = new EventEmitter<void>();
  @Output() saveGraph = new EventEmitter<void>();

  private unsubscribe: Subject<void> = new Subject();

  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  projectId: number;
  userId: number;
  udfNameList: any[];

  public selectedUdfFunction: any;
  // public isPreview: boolean;
  // public udfNameList:Array<Object> = [
  //   {id: 1, name: 'Addition'},
  //   {id: 2, name: 'Substraction'},
  //   {id: 3, name: 'Multiplication'}
  // ];

  // public udfNameList = [{
  //   id: 1,
  //   name: 'Addition'
  // },
  // {
  //   id: 1,
  //   name: 'Substraction'
  // },
  // {
  //   id: 1,
  //   name: 'Multiplication'
  // }];

  constructor(
    private activatedRoute: ActivatedRoute,
    private engineeringService: EngineeringService,
    private quantumFacade: Quantumfacade,
    private router: Router
  ) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
        this.userId = +this.certificateData.user_id;
      });

  }


  ngOnInit(): void {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');

    this.loadFolders();
    this.getUdfData();
  }

  addTotal(): void {
    this.addTotalElement.emit();
  }

  addJoin(): void {
    this.addJoinElement.emit();
  }

  delete(): void {
    this.deleteElement.emit();
    this.selectedUdfFunction = undefined;
  }

  udfSelected(): void {
    this.udf.emit();
  }

  pgsqlSelected(): void {
    this.pgsql.emit();
  }

  addUdf(data: string): void {
    this.addUdfElement.emit();
  }

  onChangeUdf(): void {
    const event = this.selectedUdfFunction;
    this.addUdfElement.emit(event);
  }

  public back(): void {
    this.router.navigate([`projects/${this.projectId}/engineering`]);
  }

  private loadFolders(): void {
    this.engineeringService.getFolders(this.userId, this.projectId).subscribe((response) => {
      console.log(response);

    }, () => {
      // this.folders = [];
    });
  }

  private getUdfData(): void {
    this.engineeringService.getUdfData(this.projectId, this.userId).subscribe((res) => {
      this.udfNameList = res.result;
    }, () => {
    });
  }
}
