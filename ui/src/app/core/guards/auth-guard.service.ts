import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { Certificate } from '../../models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  certificate$: Observable<Certificate>;

  constructor(
    private router: Router,
    private quantumFacade: Quantumfacade) {
    this.certificate$ = this.quantumFacade.certificate$;
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const certificate = JSON.parse(localStorage.getItem('certificate'));

    if (certificate) {
      return true;
    } else {
      this.router.navigate(['/login'], {
        queryParams: {
          return: state.url
        }
      });
      return false;
    }
  }

  rerunGuradsAndResolvers() {
    const defaltOnSameUrlNavigation = this.router.onSameUrlNavigation;
    this.router.onSameUrlNavigation = 'reload';
    this.router.navigateByUrl(this.router.url, {
      replaceUrl: true
    });
    this.router.onSameUrlNavigation = defaltOnSameUrlNavigation;
  }
}
