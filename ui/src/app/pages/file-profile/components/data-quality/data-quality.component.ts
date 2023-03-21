import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { DataQuality } from '../../models/file-stats';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-data-quality',
  templateUrl: './data-quality.component.html',
  styleUrls: ['./data-quality.component.scss']
})
export class DataQualityComponent {

  public isLock: boolean;
  public errorMessage: string;
  public projectId: number;
  public folderId: number;
  public fileId: number;
  public loading: boolean;
  public dataQuality: DataQuality;
  public userId: number;
  certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();

  constructor(
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private profileService: ProfileService) {

    this.projectId = +this.activatedRoute.parent.snapshot.paramMap.get('projectId');

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((certificate: Certificate) => {
        this.userId = +certificate.user_id;
      });

    this.activatedRoute.queryParams.subscribe(params => {
      this.folderId = params['folderId'];
      this.fileId = params['fileId'];

      if (this.folderId && this.fileId) {
        this.getDataQuality();
      }
    });
  }

  public getDataQuality(): void {

    this.dataQuality = {} as DataQuality;

    this.loading = true;
    this.profileService.getDataQuality(this.projectId, this.userId, this.folderId, this.fileId).subscribe((response: any) => {
      this.loading = false;
      if (response?.code === 401) {
        this.isLock = true;
        this.errorMessage = response?.message;
      } else {
        if (response?.analytics) {
          // temporary fix as per dicussion with Prakash.
          const data = response?.analytics?.replace(/NaN/g, '0.0');
          this.dataQuality = JSON.parse(data);
        }
      }
    }, () => {
      this.loading = false;
    });
  }
}
