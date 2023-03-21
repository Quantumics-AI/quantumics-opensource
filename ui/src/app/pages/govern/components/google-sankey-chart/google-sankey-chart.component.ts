import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { GovernService } from '../../services/govern.service';

@Component({
  selector: 'app-google-sankey-chart',
  templateUrl: './google-sankey-chart.component.html',
  styleUrls: ['./google-sankey-chart.component.scss']
})
export class GoogleSankeyChartComponent implements OnInit {

  title = '';
  type = 'Sankey';
  // data = [
  //   ['Brazil', 'Portugal', 5],
  //   ['Brazil', 'France', 1],
  //   ['Brazil', 'Spain', 1],
  //   ['Brazil', 'England', 1],

  //   ['Canada', 'Portugal', 1],
  //   ['Canada', 'France', 5],
  //   ['Canada', 'England', 1],

  //   ['Mexico', 'Portugal', 1],
  //   ['Mexico', 'France', 1],
  //   ['Mexico', 'Spain', 5],
  //   ['Mexico', 'England', 1],

  //   ['USA', 'Portugal', 1],
  //   ['USA', 'France', 1],
  //   ['USA', 'Spain', 1],
  //   ['USA', 'England', 5],

  //   ['Portugal', 'Angola', 2],
  //   ['Portugal', 'Senegal', 1],
  //   ['Portugal', 'Morocco', 1],
  //   ['Portugal', 'South Africa', 3],

  //   ['France', 'Angola', 1],
  //   ['France', 'Senegal', 3],
  //   ['France', 'Mali', 3],
  //   ['France', 'Morocco', 3],
  //   ['France', 'South Africa', 1],

  //   ['Spain', 'Senegal', 1],
  //   ['Spain', 'Morocco', 3],
  //   ['Spain', 'South Africa', 1],

  //   ['England', 'Angola', 1],
  //   ['England', 'Senegal', 1],
  //   ['England', 'Morocco', 2],
  //   ['England', 'South Africa', 7],

  //   ['South Africa', 'China', 5],
  //   ['South Africa', 'India', 1],
  //   ['South Africa', 'Japan', 3],

  //   ['Angola', 'China', 5],
  //   ['Angola', 'India', 1],
  //   ['Angola', 'Japan', 3],

  //   ['Senegal', 'China', 5],
  //   ['Senegal', 'India', 1],
  //   ['Senegal', 'Japan', 3],

  //   ['Mali', 'China', 5],
  //   ['Mali', 'India', 1],
  //   ['Mali', 'Japan', 3],

  //   ['Morocco', 'China', 5],
  //   ['Morocco', 'India', 1],
  //   ['Morocco', 'Japan', 3]
  // ];
  data: any[];
  columnNames = ['From', 'To', 'Weight'];

  options = {};
  width = 550;
  height = 400;

  private projectId: number;
  private id: number;
  private certificate$: Observable<Certificate>;
  private userId: number;
  termName: string;
  public projectName: string;

  private unsubscribe: Subject<void> = new Subject();

  constructor(
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private governService: GovernService) {
    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((certificate: Certificate) => {
        this.userId = +certificate.user_id;
      });

    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);

    this.id = parseInt(this.activatedRoute.snapshot.paramMap.get('id'), 10);

  }


  ngOnInit(): void {
    this.governService.getSnAttributes(this.projectId, this.userId, this.id).subscribe(res => {
      if (res.code === 200) {
        this.data = JSON.parse(res.result);
        var name = this.data[0];
        this.termName = name[0];
      }
    });
    this.projectName = localStorage.getItem('projectname');
  }

  closeCurrentWindow(): void {
    window.close();
  }
}
