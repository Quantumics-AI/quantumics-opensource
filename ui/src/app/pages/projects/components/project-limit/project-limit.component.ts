import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-project-limit',
  templateUrl: './project-limit.component.html',
  styleUrls: ['./project-limit.component.scss']
})
export class ProjectLimitComponent implements OnInit {
  @Input() projectId: number;

  public loading: boolean;
  constructor(
    public modal: NgbActiveModal,
  ) { }

  ngOnInit(): void {
    
  }

  public close(): void {
    setTimeout(() => this.modal.close(), 10);
  }

}
