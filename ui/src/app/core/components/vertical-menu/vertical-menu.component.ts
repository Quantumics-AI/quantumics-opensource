import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject, Subscription } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SharedService } from 'src/app/core/services/shared.service';
import { Certificate } from 'src/app/models/certificate';
import { Project } from 'src/app/models/project';
import { ProjectComponent } from 'src/app/pages/projects/components/project/project.component';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';
import { ProjectsService } from 'src/app/pages/projects/services/projects.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { NotificationService } from '../notification/services/notification.service';
import { IMenu } from 'src/app/models/menu';

declare let window: any;

@Component({
  selector: 'app-vertical-menu',
  templateUrl: './vertical-menu.component.html',
  styleUrls: ['./vertical-menu.component.scss']
})
export class VerticalMenuComponent implements OnInit, OnDestroy {

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();
  private subscription: Subscription;

  public projects: Array<any>;
  public projectColors = [
    '#97C590', '#FE7151', '#7C6EFF', '#3B5441',
    '#8DDDFF', '#FADC40', '#2D9F84', '#68C171',
    '#EA5656', '#6C4772', '#214179'];

  public projectId: number;
  state$: Observable<any>;
  userId: number;
  workspaceId: string;
  private isCreateProject: string;

  public isAdmin: boolean;
  public userType: boolean;
  public menus: { [name: string]: IMenu; } = {};

  constructor(
    private projectsService: ProjectsService,
    private sharedService: SharedService,
    private router: Router,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal
  ) {

    this.sharedService.deActiveWorkSpace.subscribe((projectId: number) => {
      const selectedProject = this.projects.find(x => x.projectId === projectId);
      if (selectedProject) {
        selectedProject.isDeleted = true;
      }
    });

    this.sharedService.workSpaceUpdate.subscribe((project: Project) => {
      this.projects.map(t => t.markAsDefault = false);
      const selectedProject = this.projects.find(x => x.projectId === project.projectId);
      if (selectedProject) {
        selectedProject.projectDisplayName = project.projectDisplayName;
        selectedProject.markAsDefault = project.markAsDefault;
        selectedProject.isDeleted = project.isDeleted;

        if (project.markAsDefault) {
          this.projects.sort((a, b) => b.markAsDefault - a.markAsDefault);
        }

        this.onWorkSpaceChange(selectedProject.projectId);
      }
    });

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        if (certificate) {
          this.userId = +certificate.user_id;
          this.isAdmin = certificate?.userRole?.toLowerCase() === 'admin';
          this.userType = certificate?.userType?.toLocaleLowerCase() === 'aws';
        }
      });
  }

  checkProjectId() {
    this.projectId = null;
  }

  ngOnInit(): void {
    this.projectsList();
  }

  private projectsList(projectId: number = null): void {
    const param = {
      userId: this.userId.toString(),
      folderId: '',
      folderName: '',
      projectId: '',
      file: ''
    } as ProjectParam;

    this.projectsService.getProjects(param).subscribe((response) => {
      this.projects = response.result;
      // this.projects.sort((a, b) => b.markAsDefault - a.markAsDefault);
      var d = response.result.find(i => i.markAsDefault === true);
      if (d) {
        this.projects.sort((a, b) => b.markAsDefault - a.markAsDefault);

      } else {
        this.projects.sort((a, b) => {
          return (new Date(a.createdDate) as any) - <any>new Date(b.createdDate);
        });
      }

      if (window.location.pathname === '/projects') {
        if (this.projects.length) {
          this.workspaceId = projectId ?? this.projects[0].projectId;
          this.onWorkSpaceChange(this.workspaceId);
        } else {
          this.projectId = 0;
          this.workspaceId = 'Workspace';
          this.onWorkSpaceChange(this.workspaceId);
        }
      } else {
        if (window.location.pathname.includes('account')) {
          this.projectId = +localStorage.getItem('project_id');
        } else {
          if (projectId) {
            this.onWorkSpaceChange(projectId.toString());
          } else {
            this.projectId = +window.location.pathname.split('/')[2];
          }
        }

        this.workspaceId = this.projects.find(x => x.projectId === +this.projectId)?.projectId;
      }
    }, (reason) => { });
  }

  public onWorkSpaceChange(workspaceId: string): void {

    if (workspaceId === 'Workspace') {
    } else if (workspaceId == 'all' && this.isAdmin) {
      this.router.navigate([`workspaces`]);
    }
    else {
      this.projectId = +workspaceId;
      const project = this.projects.find(x => x.projectId === this.projectId);
      localStorage.setItem('projectname', project.projectDisplayName);
      localStorage.setItem('project_id', project.projectId);
      localStorage.setItem('processedDb', project.processedDb);
      localStorage.setItem('expired', project.validDays > 0 ? 'false' : 'true');
      localStorage.setItem('project-deactived', project.isDeleted);

      if (project.isDeleted) {
        this.router.navigate([`projects/${project.projectId}/restored`], { relativeTo: this.activatedRoute });
      }
      else if (project.validDays < 0) {
        this.router.navigate([`projects/${project.projectId}/expired`]);
        return;
      } else {
        this.router.navigateByUrl('', { skipLocationChange: true }).then(() => {
          this.router.navigate([`projects/${project.projectId}/stats`]);
        });
      }
    }
  }

  public onWorkSpaceCreate(): void {
    const modalRef = this.modalService.open(ProjectComponent, { size: 'md', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.isClosed = this.projects?.length > 0;

    modalRef.result.then((response) => {
      if (response.code === 200) {
        this.projectId = +response.result.projectId;
        this.projectsList(this.projectId);
      }
    }, (reason) => {
      this.projectsList();
      console.log(reason);
    });
  }

  public expandCollapseMenu(menuName: string) {
    for (let key in this.menus) {
      if (key === menuName) {
        this.menus[key].collapse = !this.menus[key]?.collapse;
      } else {
        this.menus[key].collapse = false;
      }
    }
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
