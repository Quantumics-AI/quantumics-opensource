import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { WorkspaceService } from 'src/app/pages/workspace-settings/services/workspace.service';

@Component({
  selector: 'app-rename-workspace',
  templateUrl: './rename-workspace.component.html',
  styleUrls: ['./rename-workspace.component.scss']
})
export class RenameWorkspaceComponent implements OnInit {

  @Input() project: any;
  @Input() userId: number;

  public fg: FormGroup;
  public invalidPattern: boolean;
  public loading: boolean;

  constructor(
    private workspaceService: WorkspaceService,
    public modal: NgbActiveModal,
    private snakbar: SnackbarService,
    private fb: FormBuilder) { }

  ngOnInit(): void {
    this.fg = this.fb.group({
      projectName: [this.project.projectDisplayName, [Validators.required, Validators.pattern(/^([A-Za-z0-9_]).([A-Za-z0-9_]+\s)*[A-Za-z0-9_]+$/)]],
      projectDescription: [this.project.projectDesc],
    });
  }

  public modelChangeUpdateWorkspaceName(str: string): void {

    if (this.fg.controls.projectName.value !== '') {

      const validWorkSpaceName = String(str)
        .toLowerCase().match(/^([A-Za-z0-9_]).([A-Za-z0-9_]+\s)*[A-Za-z0-9_]+$/);

      if (validWorkSpaceName == null) {
        this.invalidPattern = true;

      } else {
        this.invalidPattern = false;
      }
    } else {
      this.invalidPattern = false;
    }
  }

  private get f() {
    return this.fg.controls;
  }

  public save(): void {
    this.loading = true;
    this.workspaceService.saveProject(this.project.projectId, this.f.projectName.value, this.f.projectDescription.value, this.userId).subscribe((response) => {
      this.loading = false;
      this.snakbar.open(response.message);
      if (response.code === 200) {
        this.modal.close(response.result);
        localStorage.setItem('projectname', this.f.projectName.value);
      }
    }, (error) => {
      this.snakbar.open(error);
      this.loading = false;
    });
  }
}
