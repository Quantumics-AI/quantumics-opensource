import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Workspace } from './models/workspace';
import { ProjectComponent } from '../projects/components/project/project.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
// import { Project } from 'src/app/models/project';
import { ProjectsService } from 'src/app/pages/projects/services/projects.service';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';
import { RenameWorkspaceComponent } from './components/rename-workspace/rename-workspace.component';
import { ConfirmationModalComponent } from '../workspace-settings/components/confirmation-modal/confirmation-modal.component';
import { Project } from 'src/app/models/project';
import { WorkspaceService } from '../workspace-settings/services/workspace.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { SharedService } from 'src/app/core/services/shared.service';


@Component({
  selector: 'app-all-workspaces',
  templateUrl: './all-workspaces.component.html',
  styleUrls: ['./all-workspaces.component.scss']
})
export class AllWorkspacesComponent implements OnInit {
  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();

  public workSpaces: Array<Workspace> = [];
  public projects: Array<any>;
  public searchText: string;
  public workSpaceColors = [
    '#97C590', '#FE7151', '#7C6EFF', '#3B5441',
    '#8DDDFF', '#FADC40', '#2D9F84', '#68C171',
    '#EA5656', '#6C4772', '#214179'];

  public projectId: number;
  public isAdmin: boolean;
  public userType: boolean;
  public sortBy: string = 'dateCreated';
  public isDescending: boolean;
  private isCreateProject: string;
  private userId: number;

  constructor(
    private projectsService: ProjectsService,
    private modalService: NgbModal,
    private router: Router,
    private quantumFacade: Quantumfacade,
    private workspaceService: WorkspaceService,
    private snakbar: SnackbarService,
    private sharedService: SharedService,
    private activatedRoute: ActivatedRoute
  ) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        if (certificate) {
          this.userId = +certificate.user_id;
          this.isAdmin = certificate?.userRole?.toLowerCase() === 'admin';
          this.userType = certificate?.userType?.toLocaleLowerCase() === 'aws';

          if (!this.isAdmin) {
            this.router.navigate([`projects`]);
            return;
          }
        }
      });
      this.projectId = +localStorage.getItem('project_id');
  }

  ngOnInit(): void {
    this.projectsList();
  }

  private projectsList(): void {
    const param = {
      userId: this.userId.toString(),
      folderId: '',
      folderName: '',
      projectId: '',
      file: ''
    } as ProjectParam;

    this.projectsService.getProjects(param).subscribe((response) => {
      this.projects = response.result;
      var d = response.result.find(i=> i.markAsDefault === true);
      if (d) {
        this.projects.sort((a,b) => b.markAsDefault - a.markAsDefault);
        
      } else {
        this.projects.sort((a,b) =>{
          return (new Date(a.createdDate) as any) - <any>new Date(b.createdDate);
        });
      }
      
      // this.projects.sort((a, b) => b.markAsDefault - a.markAsDefault);
    }, (reason) => { });
  }

  public onProjectLimitReach(): void {
    this.projectsService.getProjectuserinfo(0, this.userId).subscribe((res) => {
      this.isCreateProject = res.result.createProject;
      if (this.isCreateProject?.toLowerCase() === 'false') {
      } else {
        this.onWorkSpaceCreate();
      }
    }, () => {
      this.projectsList();
    })
  }

  public onWorkSpaceCreate(): void {
    const modalRef = this.modalService.open(ProjectComponent, { size: 'md', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.isClosed = this.projects?.length > 0;

    modalRef.result.then((response) => {
      if (response.code === 200) {
        this.projectsList();
      }
    }, (reason) => {
      this.projectsList();
      console.log(reason);
    });
  }

  public onRenameWorkSpace(project: any): void {

    const modalRef = this.modalService.open(RenameWorkspaceComponent, { size: 'md' });
    modalRef.componentInstance.project = project;
    modalRef.componentInstance.userId = this.userId;

    modalRef.result.then((result) => {
      project.projectDisplayName = result.projectDisplayName;
    });
  }

  public onWorkspaceDeactivate(project: any): void {
    const modalRef = this.modalService.open(ConfirmationModalComponent, { size: 'md modal-dialog-centered', scrollable: false });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = project.projectId;
    modalRef.result.then((result) => {
      project.isDeleted = true;
    }, () => { });
  }

  public markAsDefaultWorkSpace(project: Project): void {
    this.workspaceService.saveProject(project.projectId, project.projectDisplayName, project.projectDesc, this.userId, true).subscribe((response: any) => {
      this.snakbar.open(response.message);

      if (response.code === 200) {
        this.projects.map(t => t.markAsDefault = false);
        project.markAsDefault = true;
        this.projectsList();
      }
    }, (error) => {
      this.snakbar.open(`${error}`);
    });
  }

  public redirectToPlans(projectId: number): void {
    localStorage.setItem('project_id', `${projectId}`);
    this.router.navigate([`projects/${projectId}/workspace-setting/plans`]);
  }

  public orderBy(): void {

    if (this.sortBy === 'dateCreated' && this.isDescending) {
      this.projects = this.projects.sort((a, b) => {
        return new Date(a.createdDate) as any - <any>new Date(b.createdDate);
      });
    } else if (this.sortBy === 'dateCreated' && !this.isDescending) {
      this.projects = this.projects.sort((a, b) => {
        return (new Date(b.createdDate) as any) - <any>new Date(a.createdDate);
      });
    } else if (this.sortBy === 'workspaceName' && this.isDescending) {
      this.projects = this.projects.sort((a, b) => {
        return a.projectDisplayName.localeCompare(b.projectDisplayName);
      });
    } else if (this.sortBy === 'workspaceName' && !this.isDescending) {
      this.projects = this.projects.sort((a, b) => {
        return b.projectDisplayName.localeCompare(a.projectDisplayName)
      });
    }
  }

  public sortWorkspace(s: string): void {
    if(s == 'dateCreated'){
      this.sortBy = s;
      this.projects = this.projects.sort((a, b) => {
        return (new Date(a.createdDate) as any) - <any>new Date(b.createdDate);
      });
    } else if(s == 'workspaceName'){
      this.sortBy = s;
      this.projects = this.projects.sort((a, b) => {
        return a.projectDisplayName.localeCompare(b.projectDisplayName);
      });
    } 
    
  }

  public projectRestore(projectId: number): void {
    this.projectsService.restoreProject(projectId).subscribe((res) => {
      if (res.code === 200) {
        localStorage.removeItem('project-deactived');
        this.snakbar.open(res.message);
        this.projectsList();
        this.router.navigate([`workspaces`]);
      }
    }, (error) => {
      this.snakbar.open(error);
    });
  }

  public redirectPage(project: Project): void {
    this.sharedService.workSpaceUpdate.emit(project);
  }
}
