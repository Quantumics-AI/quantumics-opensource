import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProjectValidityGuard implements CanActivate {

  constructor(
    private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const urlProjectId = parseInt(state?.url?.split('/')[2], 10);
    const currentProjectId: number = parseInt(localStorage.getItem('project_id'), 10);
    const expired = localStorage.getItem('expired') === 'true' || localStorage.getItem('expired') === null;
    const restored = localStorage.getItem('project-deactived') === 'true';

    if (state?.url.endsWith('plans') || state?.url.includes('subscription-success')) {
      return true;
    }

    if (restored && !state?.url.endsWith('restored')) {
      this.router.navigate([`/projects/${currentProjectId}/restored`]);
    } else if (expired && !state?.url.endsWith('expired')) {
      this.router.navigate([`projects/${currentProjectId}/expired`]);
    } else if ((urlProjectId !== currentProjectId)) {
      this.router.navigate([`/projects/${currentProjectId}/stats`]);
    } else {
      return true;
    }
  }

}
