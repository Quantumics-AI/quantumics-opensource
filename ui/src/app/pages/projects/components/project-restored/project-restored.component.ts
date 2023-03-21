import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { ProjectsService } from '../../services/projects.service';

@Component({
  selector: 'app-project-restored',
  templateUrl: './project-restored.component.html',
  styleUrls: ['./project-restored.component.scss']
})
export class ProjectRestoredComponent implements OnInit {

  @Output() projectRestored = new EventEmitter<boolean>();

  private projectId: number;
  constructor(
    private projectsService: ProjectsService,
    private router: Router,
    private snakbar: SnackbarService,
    private activatedRoute: ActivatedRoute) {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
  }

  ngOnInit(): void {
    localStorage.setItem('project-deactived', 'true');
  }

  public projectRestore(): void {
    this.projectsService.restoreProject(this.projectId).subscribe((res) => {
      if (res.code === 200) {
        localStorage.removeItem('project-deactived');
        this.snakbar.open(res.message);
        this.router.navigate([`projects/${this.projectId}/stats`]);
      }
    }, (error) => {
      this.snakbar.open(error);
    });
  }

}
