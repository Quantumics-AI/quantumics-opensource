import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AddComponent } from '../engineering/components/add/add.component'
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { SharedService } from './services/shared.service';

@Component({
  selector: 'app-engineering',
  templateUrl: './engineering.component.html',
  styleUrls: ['./engineering.component.scss']
})
export class EngineeringComponent implements OnInit {
  public projectName: string;
  public projectId: number;
  public userId: number;

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
  ) {
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });
  }

  ngOnInit(): void {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
    this.projectName = localStorage.getItem('projectname');
  }

  
}
