import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SharedService } from 'src/app/core/services/shared.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { PlansService } from '../../services/plans.service';

@Component({
  selector: 'app-success',
  templateUrl: './success.component.html',
  styleUrls: ['./success.component.scss']
})
export class SuccessComponent implements OnInit {
  public projectId: number;
  public loading = false;
  public paymentDetails: any;
  private userId: number;
  private sessionId: string;
  private unsubscribe: Subject<void> = new Subject<void>();
  constructor(
    private quantumFacade: Quantumfacade,
    private plansService: PlansService,
    private snakbar: SnackbarService,
    private activatedRoute: ActivatedRoute,
    private sharedService: SharedService,
    private router: Router) {
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    this.projectId = +localStorage.getItem('project_id');
    this.sessionId = this.activatedRoute.snapshot.queryParamMap.get('session_id');
  }

  ngOnInit(): void {
    this.loading = true;

    this.plansService.getSuccessDetails(this.userId, this.sessionId).subscribe((res) => {
      if (res.code === 200) {
        this.paymentDetails = res.result;
        this.loading = false;
      } else {
        this.snakbar.open('Something went wrong, please contact support');
        this.loading = false;
      }
    }, (error) => {
      this.snakbar.open('Something went wrong, please contact support');
      this.loading = false;
    });
    this.sharedService.pushProjectId.emit();
  }

  public download(): void {
    const downloadLink = document.createElement('a');
    downloadLink.href = this.paymentDetails.invoicePdf;
    downloadLink.download = '';
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
  }

  public ok(): void {
    this.router.navigate([`/projects/${this.projectId}/stats`]);
  }
}
