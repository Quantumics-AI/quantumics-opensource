import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Certificate } from 'src/app/models/certificate';

@Component({
  selector: 'app-automation',
  templateUrl: './automation.component.html',
  styleUrls: ['./automation.component.scss']
})
export class AutomationComponent implements OnInit {
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();

  userId: number;
  projectId: number;
  projectName: string;

  constructor(
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute) {

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    this.projectId = parseInt(this.activatedRoute.snapshot.paramMap.get('projectId'), 10);
  }

  ngOnInit(): void {
    this.projectName = localStorage.getItem('projectname');
  }
}
