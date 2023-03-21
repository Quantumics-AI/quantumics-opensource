import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { EngineeringService } from '../../services/engineering.service';
import { SharedService } from '../../services/shared.service';

@Component({
  selector: 'app-engineer-container',
  templateUrl: './engineer-container.component.html',
  styleUrls: ['./engineer-container.component.scss']
})
export class EngineerContainerComponent implements OnInit {

  public userId: number;
  public projectId: number;
  public projectName: string;
  public flows: any;
  public loading: boolean;

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private engrngService: EngineeringService,
    private sharedService: SharedService,
    private snackbar: SnackbarService,
    private router: Router) {
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });
    this.projectId = +localStorage.getItem('project_id');
    this.getFlows();
  }

  ngOnInit(): void {
    this.sharedService.flow.subscribe((request) => {
      if (request && !request.projectId) {
        this.createFlow(request);
      }
    });
  }

  private getFlows(): void {
    this.loading = true;
    this.engrngService
      .getEngineeringList(this.userId, this.projectId)
      .pipe(
        takeUntil(this.unsubscribe),
        map(res => res.result)
      ).subscribe((res) => {
        this.loading = false;
        this.flows = res;
      }, () => {
        this.loading = false;
      });
  }

  private createFlow(flow: any): void {
    flow.projectId = this.projectId;
    flow.userId = this.userId;
    this.engrngService.saveFlow(flow)
      .subscribe((response: any) => {
        this.snackbar.open(response.message);
        if(response.code == 200) {
          const flowId = response.result.engFlowId;
          const flowName = response.result.engFlowName;
          const flowDisplayName = response.result.engFlowDisplayName;
          this.router.navigate([`projects/${this.projectId}/engineering/engineer/${flowId}/edit/${flowDisplayName}`]);
        }
        
        this.getFlows();
      });
  }

  deleteFlow(flowId: number): void {
    this.engrngService.deleteFlow(this.projectId, this.userId, flowId).subscribe((res) => {
      this.snackbar.open(res.message);
      this.getFlows();
    });
  }
}
