import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { ProjectsService } from 'src/app/pages/projects/services/projects.service';

@Component({
  selector: 'app-confirmation-modal',
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./confirmation-modal.component.scss']
})
export class ConfirmationModalComponent implements OnInit {
  @Input() projectId: number;

  constructor(
    public modal: NgbActiveModal,
    private projectsService: ProjectsService,
    private snakbar: SnackbarService,
  ) {
  }

  ngOnInit(): void {
  }

  public deactivateProject(): void {
    this.projectsService.deleteProject(this.projectId).subscribe((response: any) => {
      this.snakbar.open(response.message);
      if (response.code === 200) {
        this.modal.close(response.result);
      }
    }, (error) => {
      this.snakbar.open(error);
    });
  }
}
