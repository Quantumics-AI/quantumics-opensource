import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Certificate } from 'src/app/models/certificate';
import { takeUntil } from 'rxjs/operators';
import { SystemStatusService } from './services/system-status.service';

@Component({
  selector: 'app-system-status',
  templateUrl: './system-status.component.html',
  styleUrls: ['./system-status.component.scss']
})
export class SystemStatusComponent implements OnInit {

  @Output() close = new EventEmitter<boolean>();
  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject<void>();
  loading: boolean;

  systemStatus: Array<any>;
  incidents: Array<any>;
  constructor(
    private systemStatusService: SystemStatusService,
    private quantumFacade: Quantumfacade) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((certificate: Certificate) => {
        this.certificateData = certificate;
      });
  }

  ngOnInit(): void {
    this.loading = true;
    this.systemStatusService.getSystemStatus(+this.certificateData.user_id).subscribe((response) => {
      this.loading = false;
      if (response.code === 200) {
        this.systemStatus = response.result.system_status;
        this.incidents = response.result.incidents;
      }
    }, (error) => {
      this.loading = false;
      this.systemStatus = [];
      this.incidents = [];
    });
  }
}
