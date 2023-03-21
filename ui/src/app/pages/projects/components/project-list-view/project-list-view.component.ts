import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';

@Component({
  selector: 'app-project-list-view',
  templateUrl: './project-list-view.component.html',
  styleUrls: ['./project-list-view.component.scss']
})
export class ProjectListViewComponent implements OnInit {

  @Input() projects: any;
  @Output() selectedProject = new EventEmitter<any>();
  @Output() redirectToSetting = new EventEmitter<string>();
  @Output() deleteProject = new EventEmitter<string>();
  @Output() redirectToPlans = new EventEmitter<string>();
  @Output() restoreProject = new EventEmitter<string>();
  @Output() redirectToAwsPlans = new EventEmitter<string>();

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();

  public isAdmin: boolean;
  public userType: boolean;

  constructor(private quantumFacade: Quantumfacade) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$.pipe(takeUntil(this.unsubscribe)).subscribe(certificate => {
      if (certificate) {
        this.isAdmin = certificate?.userRole?.toLowerCase() === 'admin';
        this.userType = certificate?.userType?.toLocaleLowerCase() === 'aws'
      }
    });
  }

  ngOnInit(): void {
  }

  public isDisabled(project: any): boolean {
    return project?.validDays <= 0 || project?.isDeleted;
  }

}
