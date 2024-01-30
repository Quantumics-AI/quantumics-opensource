import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Certificate } from 'src/app/models/certificate';
import { GovernService } from './services/govern.service';
import { FolderService } from '../file-profile/services/folder.service';

@Component({
  selector: 'app-govern',
  templateUrl: './govern.component.html',
  styleUrls: ['./govern.component.scss']
})
export class GovernComponent implements OnInit, OnDestroy {
  private unsubscribe: Subject<void> = new Subject();
  certificate$: Observable<Certificate>;

  projectId: number;
  loading: boolean;
  businessData$: Observable<any>;
  projectName: string;
  userId: number;
  public hasFolders: boolean;

  constructor(
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private governService: GovernService,
    private router: Router,
    private folderService: FolderService,) {
    this.projectId = +this.activatedRoute.snapshot.params['projectId'];

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((certificate: Certificate) => {
        this.userId = +certificate.user_id;
      });
  }

  ngOnInit(): void {
    this.projectName = localStorage.getItem('projectname');
    this.foldersList();
    // this.loadData();
  }

  foldersList() {
    this.loading = true;
    this.folderService.getFolders(this.projectId, this.userId)
    .pipe(takeUntil(this.unsubscribe))
    .subscribe((response: any) => {
      this.hasFolders = response.result?.length;
      console.log(response.result?.length);
      this.loading = false;
      if (this.hasFolders) {
        // this.getProjectStats();
        this.loadData();
      }
    }, () => {
      this.hasFolders = false;
    });
  }

  loadData(): void {
    this.loading = true;
    this.businessData$ = this.governService.getGlossaryList(this.projectId, this.userId)
    .pipe(takeUntil(this.unsubscribe))
    .pipe(
      map((res: any) => {
        this.loading = false;
        if (res?.result.length) {
          this.router.navigate(['dashboard/list'], {
            relativeTo: this.activatedRoute
          });
        }
        return res.result;
      }, () => {
        this.loading = false;
      }));
  }

  redirectToCreate(): void {
    this.router.navigate(['dashboard/list'], {
      relativeTo: this.activatedRoute,
      queryParams: { isNew: 1 }
    });
  }

  ngOnDestroy() {
    this.unsubscribe.unsubscribe();
  }
}
