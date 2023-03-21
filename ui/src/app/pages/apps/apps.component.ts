import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';

import { takeUntil } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { ProjectParam } from '../projects/models/project-param';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Certificate } from 'src/app/models/certificate';

import { AppConfiguration } from 'src/AppConfiguration.interface';

declare let appConfiguration: AppConfiguration;

const isWindowContext = typeof window !== 'undefined';

@Component({
  selector: 'app-apps',
  templateUrl: './apps.component.html',
  styleUrls: ['./apps.component.scss']
})
export class AppsComponent implements OnInit {

  urlSafe: SafeResourceUrl;
  results: any;
  projectParam: ProjectParam;
  certificate$: Observable<Certificate>;
  certificateData: Certificate;
  projectId: string;
  projectSource$: Observable<any>;
  private unsubscribe: Subject<void> = new Subject();
  folders: any;

  constructor(
    public sanitizer: DomSanitizer,
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade
  ) {
    window.addEventListener('message', this.receiveMessage);
    this.projectSource$ = this.quantumFacade.projectSource$;
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.certificateData = certificate;
      });
  }

  ngOnInit(): void {
    this.projectId = this.activatedRoute.parent.snapshot.paramMap.get('projectId');
    const certificate: Certificate = JSON.parse(localStorage.getItem('certificate'));

    this.urlSafe = this.sanitizer
      .bypassSecurityTrustResourceUrl(`${appConfiguration.redashUrl}/login/${this.certificateData.redash_key}/${this.projectId}/${this.certificateData.user_id}/${certificate.token}/${btoa(appConfiguration.apiUrl)}?v=${Date.now()}`);
  }

  receiveMessage(event) {
    if (event.origin !== 'http://localhost:4200') {
      console.log('Event: ', event);
      this.results = event;
    }
  }

  sendMessage(query: string | null) {
    // console.log('send message', query);
    const iframeEl = document.getElementById('redash') as HTMLIFrameElement;
    const credentials = query ? query : 'Hello from Quantum Spark';
    iframeEl.contentWindow.postMessage(credentials, '*');
  }

}
