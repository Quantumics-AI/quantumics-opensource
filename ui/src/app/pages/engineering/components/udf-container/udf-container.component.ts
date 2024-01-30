import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-udf-container',
  templateUrl: './udf-container.component.html',
  styleUrls: ['./udf-container.component.scss']
})
export class UdfContainerComponent {

  public projectId: number;
  public projectName: string;

  constructor(private activatedRoute: ActivatedRoute) {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
    this.projectName = localStorage.getItem('projectname');
  }
}
