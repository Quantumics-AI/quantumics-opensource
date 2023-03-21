import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { WorkspaceService } from '../../services/workspace.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { Observable, Subject } from 'rxjs';
import { ConfirmationDialogComponent } from 'src/app/core/components/confirmation-dialog/confirmation-dialog.component';
import { Project } from 'src/app/models/project';

@Component({
  selector: 'app-workspace-details',
  templateUrl: './workspace-details.component.html',
  styleUrls: ['./workspace-details.component.scss']
})
export class WorkspaceDetailsComponent implements OnInit {
  projectId: number;
  private hasFormChanged: boolean;
  private selectedProject: Project;
  fg: FormGroup;
  loading: boolean;
  userId: number;
  projectLogoName: string;
  userType: boolean;
  public isAdmin: boolean;
  private unsubscribe: Subject<void> = new Subject<void>();

  invalidPattern: boolean = false;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private quantumFacade: Quantumfacade,
    private snakbar: SnackbarService,
    private workspaceService: WorkspaceService,
    private fb: FormBuilder,
    private sharedService: SharedService
  ) {
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = +certificate.user_id;
        this.userType = certificate.userType?.toLowerCase() === "aws";
        this.isAdmin = certificate.userRole?.toLowerCase() === "admin";
      });
  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      projectName: ['', [Validators.required, Validators.pattern(/^([A-Za-z0-9_]).([A-Za-z0-9_]+\s)*[A-Za-z0-9_]+$/)]],
      projectDescription: [''],
      markAsDefault: [false]
    });
    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);

    this.workspaceService.getProject(this.projectId, this.userId).subscribe((response: any) => {

      this.selectedProject = response.result as Project;
      this.loading = false;

      this.f.projectName.setValue(this.selectedProject.projectDisplayName);
      this.f.projectDescription.setValue(this.selectedProject.projectDesc);
      this.f.markAsDefault.setValue(this.selectedProject.markAsDefault);

      this.fg.valueChanges.subscribe((data) => {
        this.hasFormChanged = false;
        if (this.selectedProject.projectName !== data.projectName) {
          this.hasFormChanged = true;
        } else if (this.selectedProject.projectDesc !== data.projectDescription) {
          this.hasFormChanged = true;
        } else if (this.selectedProject.markAsDefault !== data.markAsDefault) {
          this.hasFormChanged = true;
        }
      });

    }, () => {
      this.loading = false;
    });
  }

  get f() {
    return this.fg.controls;
  }

  save(): void {
    this.loading = true;
    const markAsDefault = this.f.markAsDefault.value;
    this.workspaceService.saveProject(this.projectId, this.f.projectName.value, this.f.projectDescription.value, this.userId, markAsDefault).subscribe((response) => {
      this.selectedProject.projectName = this.selectedProject.projectDisplayName = this.f.projectName.value;
      this.selectedProject.projectDesc = this.f.projectDescription.value;
      this.selectedProject.markAsDefault = this.f.markAsDefault.value;
      this.hasFormChanged = false;
      this.sharedService.workSpaceUpdate.emit(this.selectedProject);
      this.loading = false;
      localStorage.setItem('projectname', this.f.projectName.value);
      this.snakbar.open('Project details updated successfully');
    }, (error) => {
      this.snakbar.open(error);
      this.loading = false;
    });
  }

  modelChangeUpdateWorkspaceName(str) {

    // const re = /^[a-zA-Z0-9_]+$/;

    if (this.fg.controls.projectName.value != "") {

      const validWorkSpaceName = String(str)
        .toLowerCase()
        .match(
          /^([A-Za-z0-9_]).([A-Za-z0-9_]+\s)*[A-Za-z0-9_]+$/
        );

      if (validWorkSpaceName == null) {
        this.invalidPattern = true;

      } else {
        this.invalidPattern = false;
      }
    } else {
      this.invalidPattern = false;
    }

  }

  canDeactivate(): Observable<boolean> | boolean {

    if (!this.hasFormChanged) {
      return true;
    }

    return this.confirmation();

  }

  private confirmation(): Observable<boolean> {
    return new Observable((result) => {
      const modalRef = this.modalService.open(ConfirmationDialogComponent, { size: 'md' });
      modalRef.componentInstance.title = 'Are you sure?';
      modalRef.componentInstance.message = 'You are switching to another screen without saving the changes';
      modalRef.componentInstance.btnOkText = 'Save & Close';
      modalRef.componentInstance.btnCancelText = 'Discard';
      modalRef.result.then((processWithoutSave) => {

        if (!processWithoutSave) {
          result.next(true);
          result.complete();
        }
        else {
          this.loading = true;
          this.workspaceService.saveProject(this.projectId, this.f.projectName.value, this.f.projectDescription.value, this.userId, this.f.markAsDefault.value).subscribe((res) => {
            this.loading = false;
            this.snakbar.open('Project details updated successfully');
            result.next(true);
            result.complete();
          }, (error) => {
            this.snakbar.open(error);
            this.loading = false;
          });
        }
      });
    });
  }
}