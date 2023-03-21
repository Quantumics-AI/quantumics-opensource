import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { result } from 'lodash';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Project } from '../../models/project';
import { ProjectsService } from '../../services/projects.service';

@Component({
  selector: 'app-add',
  templateUrl: './add.component.html',
  styleUrls: ['./add.component.scss']
})
export class AddComponent implements OnInit {
  @Input() userId: number;
  @Output() create: EventEmitter<any> = new EventEmitter<any>();
  fg: FormGroup;
  projectLogoName: string;
  loading: boolean;

  constructor(
    private projectsService: ProjectsService,
    private snakbar: SnackbarService,
    public modal: NgbActiveModal,
    private fb: FormBuilder) { }

  ngOnInit(): void {

    this.projectLogoName = 'Upload Project Logo';

    this.fg = this.fb.group({
      projectName: new FormControl('', [Validators.required, Validators.maxLength(30), Validators.pattern(/^([A-Za-z0-9_]).([A-Za-z0-9_]+\s)*[A-Za-z0-9_]+$/)]),
      projectDesc: new FormControl('', Validators.maxLength(255))
    });
  }

  save(event): void {

    if (event.target?.files[0]?.name > 255) {
      this.projectLogoName = 'Upload Project Logo';
      alert('File name must be less then 255 characters');
      return;
    }

    this.loading = true;

    const project = {
      projectName: this.fg.value.projectName,
      projectDesc: this.fg.value.projectDesc,
      userId: this.userId.toString(),
      orgName: 'qsai',
      schema: 'qs_' + this.fg.value.projectName.toLowerCase().trim(),
      selectedAutomation: ['Cleansing'],
      selectedDataset: ['Import'],
      selectedEngineering: ['Engineering'],
      selectedOutcome: 'Business Intelligence'
    } as Project;

    this.projectsService.createProject(project).subscribe((response) => {
      this.loading = false;

      if (response.code === 200 && response.message !== 'Project Creation Failed') {
        if (event.files.length) {
          // upload file
          const file = event.files[0];
          this.projectsService.uploadProjectLogo(response?.result?.projectId, file).subscribe((res) => {
            this.projectLogoName = file?.name;
          }, () => {
            this.projectLogoName = 'Upload Project Logo';
          });
        }

        this.modal.close(true);
      }

      this.snakbar.open(response.message);
    }, (error) => {
      this.loading = false;
      this.snakbar.open(error);
    });
  }

  public validate(event) {

    if (!event.target.value) {
      this.projectLogoName = 'Upload Project Logo';
      return;
    }

    const regex = new RegExp('(.*?).(jpg|png)$');
    if (!regex.test(event.target.value.toLowerCase())) {
      event.target.value = '';
      alert('Please select only jpg or png file extension');
    } else if (event.target?.files[0]?.name?.length > 255) {
      event.target.value = '';
      alert('File name must be less than 255 characters');
      return;
    }

    this.projectLogoName = event.target?.files[0]?.name;
  }
}
