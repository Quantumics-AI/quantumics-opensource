import { Component, OnInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { map, takeUntil } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedService } from 'src/app/core/services/shared.service';
import { ProjectsService } from './services/projects.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { ConfirmationDialogComponent } from './components/confirmation-dialog/confirmation-dialog.component';
import { InformationComponent } from './components/information/information.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AddComponent } from './components/add/add.component';
import { InteractiveDemoComponent } from './components/interactive-demo/interactive-demo.component';

@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {
  projects: Array<any>;
  private certificate$: Observable<Certificate>;
  private certificateData: Certificate;
  private unsubscribe: Subject<void> = new Subject();
  private projectData$: Observable<any>;
  private userId: number;
  private isCreateProject: string;
  public loading: boolean;
  public isVisibleGrid = true;
  public isDescending: boolean;
  public userName: string;
  public userFirstName: string;
  public notificationContent: string;
  colors = ['#D386FB', '#212245', '#F96943', '#009A7E'];

  constructor(
    private projectsService: ProjectsService,
    private quantumFacade: Quantumfacade,
    private router: Router,
    private sharedService: SharedService,
    private activatedRoute: ActivatedRoute,
    private snakbar: SnackbarService,
    private modalService: NgbModal
  ) {
    this.projectData$ = this.quantumFacade.projectData$;
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$.pipe(takeUntil(this.unsubscribe)).subscribe(certificate => {
      if (certificate) {
        this.userId = +certificate.user_id;
        this.userName = certificate.user;
        this.certificateData = certificate;
      }
      var str = this.userName;
      this.userFirstName = str.split(' ')[0];
    });
  }

  ngOnInit(): void {
    // this.projectsList();
    // this.sharedService.pushProjectId.emit();
    // localStorage.removeItem('expired');
    // localStorage.removeItem('project_id');
  }

  projectsList(): void {
    this.loading = true;
    const param = {
      userId: this.certificateData.user_id,
      folderId: '',
      folderName: '',
      projectId: '',
      file: ''
    };

    this.projectsService.getProjects(param).subscribe((response) => {
      this.projects = response.result;
      this.projects.sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime());
      // this.projects = [];
      if (this.projects?.length === 0) {
        // this.openCreateProjectModal();
      } else {
        this.projects = this.formatResponse(this.projects);
      }
    }, (reason) => {
      console.log(reason);
    });
  }

  isAdmin(): boolean {
    return this.certificateData?.userRole?.toLowerCase() === 'admin';
  }

  select(project) {
    this.sharedService.pushProjectId.emit(project.projectId);
    localStorage.setItem('projectname', project.projectDisplayName);
    localStorage.setItem('project_id', project.projectId);
    localStorage.setItem('processedDb', project.processedDb);
    localStorage.setItem('expired', project.validDays > 0 ? 'false' : 'true');

    this.router.navigate([`${project.projectId}/stats`], { relativeTo: this.activatedRoute });
  }

  deleteProject(projectId: number): void {

    const modalRef = this.modalService.open(ConfirmationDialogComponent, { size: 'sm' });
    modalRef.componentInstance.title = 'Confirmation';
    modalRef.componentInstance.message = 'Are you sure you want to delete the Project?';
    modalRef.componentInstance.btnOkText = 'Delete';
    modalRef.componentInstance.btnCancelText = 'Cancel';

    modalRef.result.then((result) => {
      if (result) {
        // Delete the project.
        this.projectsService.deleteProject(projectId).subscribe((res) => {
          if (res?.code === 200) {
            this.snakbar.open(res.message);
            this.projectsList();
          }
        }, (error) => {
          this.snakbar.open(error);
        });
      }
    }, (reason) => {
      console.log(reason);
    });
  }

  public gotoSettings(projectId): void {
    this.router.navigate([`${projectId}/general-setting`], { relativeTo: this.activatedRoute });
  }

  private information(): void {
    const modalRef = this.modalService.open(InformationComponent,
      {
        size: 'lg',
        windowClass: 'modal-size'
      });
  }

  public openCreateProjectModal(): void {

    const modalRef = this.modalService.open(AddComponent, {
      backdrop: 'static',
      size: 'lg'
    });
    modalRef.componentInstance.userId = this.certificateData.user_id;

    modalRef.result.then((success) => {
      if (success) {
        this.projectsList();
        this.information();
      }
    }, (reason) => {
      console.log(reason);
    });
  }

  public isEllipsisActive(e: HTMLElement): boolean {
    return e ? (e.offsetWidth < e.scrollWidth) : false;
  }

  public restoreProject(projectId: number) {
    this.projectsService.restoreProject(projectId).subscribe((res) => {
      if (res.code === 200) {
        this.snakbar.open(res.message);
        this.projectsList();
      }
    }, (error) => {
      this.snakbar.open(error);
    });
  }


  private formatResponse(projects: any): any {

    projects = projects.map(t => {
      const top4Member = t.projectMembers.slice(0, 4).map(t1 => {
        const matches = t1.match(/\b(\w)/g);

        return {
          shortName: matches.join(''),
          fullName: t1
        };
      });

      t.firstFourMember = top4Member;
      t.totalMembers = t.projectMembers;
      t.projectMembers = t.projectMembers.slice(4, t.projectMembers?.length);
      return t;
    });

    return projects;
  }

  public createProject(popover: any): void {
    // TODO - need to update API res to return false instead of string.

    const hasAllowToCreateProject = this.certificateData?.userType?.toLowerCase() === 'aws' && this.projects?.length > 1;

    if (hasAllowToCreateProject) {
      this.notificationContent = 'You have reached the maximum number project creation limit.';
      setTimeout(() => {
        popover.open();
      }, 10);
      return;
    }

    this.notificationContent = `You have reached the maximum number of trial projects.
    To create more projects, upgrade your
    subscription plan. (If you are not owner of the project.
    Please contact your project owner)`;

    this.projectsService.getProjectuserinfo(0, this.userId).subscribe((res) => {
      this.isCreateProject = res.result.createProject;

      if (this.isCreateProject?.toLowerCase() === 'false') {
        setTimeout(() => {
          popover.open();
        }, 10);
      } else {
        this.openCreateProjectModal();
      }
    }, () => {
      setTimeout(() => {
        popover.open();
      }, 10);
    });
  }

  public redirectToSetting(project): void {
    const projectId = project.projectId;

    localStorage.setItem('expired', project.validDays > 0 ? 'false' : 'true');
    localStorage.setItem('project_id', `${projectId}`);
    this.router.navigate([`${projectId}/general-setting`], { relativeTo: this.activatedRoute });
  }

  public redirectToPlans(projectId: number): void {
    localStorage.setItem('project_id', `${projectId}`);
    this.router.navigate([`${projectId}/plans`], { relativeTo: this.activatedRoute });
  }

  public redirectToAwsPlans(url: string): void {
    window.open(url, "_blank");
  }

  public viewInteractiveDemo(url: string): void {
    // this.modalService.open(InteractiveDemoComponent, {
    //   backdrop: 'static',
    //   size: 'lg'
    // });
    window.open(url, "_blank");
  }

  public sort(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.projects = this.projects.sort((a, b) => {
        return new Date(a.createdDate) as any - <any>new Date(b.createdDate);
      });
    } else {
      this.projects = this.projects.sort((a, b) => {
        return (new Date(b.createdDate) as any) - <any>new Date(a.createdDate);
      });
    }

  }
}
