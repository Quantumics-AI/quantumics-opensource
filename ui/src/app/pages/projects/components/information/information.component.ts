import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ProjectsService } from '../../services/projects.service';

@Component({
  selector: 'app-information',
  templateUrl: './information.component.html',
  styleUrls: ['./information.component.scss']
})
export class InformationComponent implements OnInit {

  modules: any;
  data: any;

  constructor(
    public modal: NgbActiveModal,
    private projectsService: ProjectsService) { }

  ngOnInit(): void {
    this.projectsService.getModuleDetails().subscribe((response) => {
      this.data = response.result;
      if (response.result) {
        this.modules = Object.keys(response.result);
      }
    }, () => {
      this.modules = [];
    });
  }

  close(): void {
    this.modal.close();
  }

}
