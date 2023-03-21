import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { Certificate } from '../../models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  certificate: Certificate;


  constructor(
    private router: Router,
    private quantumFacade: Quantumfacade) {
    this.quantumFacade.certificate$.subscribe(d => {
      this.certificate = d;
    });
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const isAdmin = this.certificate?.userRole?.toLowerCase() === 'admin';
    if (isAdmin) {
      return true;
    } else {
      this.router.navigate(['/projects']);
    }
  }

}
