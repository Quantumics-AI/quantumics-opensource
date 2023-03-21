import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Project } from '../../models/project';
import { ProjectsService } from '../../services/projects.service';

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.scss']
})
export class ProjectComponent implements OnInit {
  @Input() userId: number;
  @Input() isClosed: boolean;

  public fg: FormGroup;
  public loading: boolean;
  invalidPattern: boolean = false;

  constructor(
    private snakbar: SnackbarService,
    public modal: NgbActiveModal,
    private fb: FormBuilder,
    private projectsService: ProjectsService) { }

  ngOnInit(): void {
    this.fg = this.fb.group({
      workSpaceName: new FormControl('', [Validators.required, Validators.maxLength(30), Validators.pattern(/^([A-Za-z0-9_]).([A-Za-z0-9_]+\s)*[A-Za-z0-9_]+$/)]),
    });
  }

  public save(): void {
    if (this.fg.invalid)
      return;

    this.loading = true;
    const project = {
      projectName: this.fg.value.workSpaceName,
      projectDesc: '',
      userId: this.userId.toString(),
      orgName: 'qsai',
      schema: 'qs_' + this.fg.value.workSpaceName.toLowerCase().trim(),
      selectedAutomation: ['Cleansing'],
      selectedDataset: ['Import'],
      selectedEngineering: ['Engineering'],
      selectedOutcome: 'Business Intelligence',
      markAsDefault: true
    } as Project;

    this.projectsService.createProject(project).subscribe((response) => {
      this.loading = false;
      this.snakbar.open(response.message);
      if (response.code === 200 && response.message !== 'Project Creation Failed') {
        this.modal.close(response);
      }
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });
  }

  modelChangeWorkspaceName(str) {

    // const re = /^[a-zA-Z0-9_]+$/;

    if (this.fg.controls.workSpaceName.value != "") {

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
}
