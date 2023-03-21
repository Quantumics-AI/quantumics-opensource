import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { Quantumfacade } from './state/quantum.facade';
import { filter, map, mergeMap, takeUntil } from 'rxjs/operators';
import { environment } from '../environments/environment';

declare let window: any;
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  private unsubscribe: Subject<void> = new Subject();
  hideNavigation = false;
  constructor(
    private router: Router,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute
  ) {

    this.router.events.pipe(
      takeUntil(this.unsubscribe),
      filter((event) => event instanceof NavigationEnd),
      map(() => this.activatedRoute),
      map((route) => {
        while (route.firstChild) {
          route = route.firstChild;
        }
        return route;
      }),
      mergeMap((route) => route.data)
    ).subscribe((event) => {
      this.hideNavigation = event.hideNavigation;
    });

  }

  ngOnInit(): void {
    const certificate = localStorage.getItem('certificate');
    if (certificate) {
      const certificateJson = JSON.parse(certificate);
      this.quantumFacade.storeCerticate(certificateJson);
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }
}
