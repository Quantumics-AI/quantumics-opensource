import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-expired',
  templateUrl: './expired.component.html',
  styleUrls: ['./expired.component.scss']
})
export class ExpiredComponent implements OnInit {
  public projectId: number;
  constructor(
    private router: Router,
    private snakbar: SnackbarService,
    private activatedRoute: ActivatedRoute
  ) {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
   }

  ngOnInit(): void {
  }

  public redirectToPlans(projectId: number): void {
    localStorage.setItem('project_id', `${projectId}`);
    this.router.navigate([`projects/${projectId}/workspace-setting/plans`]);
  }
}
