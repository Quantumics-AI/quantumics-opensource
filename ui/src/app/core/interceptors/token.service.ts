import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';
import { environment } from '@environment/environment';
import { LoginService } from 'src/app/pages/login/services/login.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Certificate } from 'src/app/models/certificate';
import { Router } from '@angular/router';
import { AppConfiguration } from 'src/AppConfiguration.interface';

declare let appConfiguration: AppConfiguration;

@Injectable({
  providedIn: 'root'
})
export class TokenInterceptor implements HttpInterceptor {

  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(
    private loginService: LoginService,
    private router: Router,
    private quantumFacade: Quantumfacade) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token: any = JSON.parse(localStorage.getItem('certificate'));
    const endpoint = req.url;
    const skipTracking = endpoint.endsWith('/count') || endpoint.includes('/preview/all');

    req = req.clone({
      url: `${appConfiguration.apiUrl}${endpoint}`
    });

    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token.token}`
          // 'Cache-Control': 'no-cache'
        }
      });
    }

    const started = Date.now();
    const foo: any = {};

    return next.handle(req).pipe(
      tap((event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {

          foo.status = event.status;
          foo.endPoint = req.url.split('?')[0];
        }
      }, (error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.handle401Error(req, next);
        }
        foo.status = error.status;
        foo.endPoint = req.url ? req.url.split('?')[0] : '';
        foo.errMsg = error.error.msg || error.statusText;
      }),
      catchError(this.handleError),
      finalize(() => {
        if (!skipTracking) {
          const elapsed = Date.now() - started;
          foo.responseTime = elapsed;
        }
      })
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage;
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = error?.error?.message;
    }
    return throwError(errorMessage);
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler) {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const certificate: Certificate = JSON.parse(localStorage.getItem('certificate'));

      if (certificate && certificate.token) {
        return this.loginService.getRefreshToken(certificate.refreshToken).subscribe((response: Certificate) => {
          this.isRefreshing = false;

          certificate.token = response.token;
          certificate.refreshToken = response.refreshToken;

          localStorage.setItem('certificate', JSON.stringify(certificate));
          this.quantumFacade.storeCerticate(certificate);

          return next.handle(this.addTokenHeader(request, certificate.token));
        }, (error) => {
          this.isRefreshing = false;
          this.router.navigate(['/login']);
          return throwError(error);
        });
      } else {
        this.router.navigate(['/login']);
        console.log('Request is in else condition');
        return throwError('End request');
      }
    }
  }

  private addTokenHeader(request: HttpRequest<any>, token: string) {
    return request.clone({ headers: request.headers.set('Bearer', token) });
  }
}
