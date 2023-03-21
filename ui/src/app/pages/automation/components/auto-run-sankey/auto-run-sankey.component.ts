import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { AutomationService } from '../../services/automation.service';

@Component({
  selector: 'app-auto-run-sankey',
  templateUrl: './auto-run-sankey.component.html',
  styleUrls: ['./auto-run-sankey.component.scss']
})
export class AutoRunSankeyComponent implements OnInit {

  title = '';
  type = 'Sankey';
  // data = [
  //   ['Brazil', 'Portugal', 5, '<div style="padding:16px; width:550px"><b>Title</b> <br/> Some more information</div>'],
  //   ['Brazil', 'France', 1, 'test'],
  //   ['Brazil', 'Spain', 1, 'test'],
  //   ['Brazil', 'England', 1, '<h1>test</h1> <br/> sdf'],
  // ];
  data: any[];
  columnNames = ['From', 'To', 'Weight'];

  options = {
    tooltip: { isHtml: true },
  };

  width = 550;
  height = 400;
  nodes: any;
  links: any;
  legends: any;
  private projectId: number;
  private id: number;
  private certificate$: Observable<Certificate>;
  private userId: number;

  private unsubscribe: Subject<void> = new Subject();

  constructor(
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private automationService: AutomationService) {
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
    this.automationService.getDataLineageInfo(this.projectId, this.userId, this.id).subscribe(res => {
      if (res.code === 200) {
        this.data = res.result;
        let nodes = this.data.map(n => n.source);
        nodes = nodes.concat(this.data.map(n => n.target));
        this.nodes = [...new Set(nodes)].map(n => ({ name: n }));

        this.legends = [...new Set(this.data.map(c => c.category))].map((n) => ({ name: n }));
        this.legends.forEach((l: any) => {
          switch (l.name) {
            case 'Folder':
              l.color = 'green';
              break;
            case 'file':
              l.color = 'yellowgreen';
              break;
            case 'join':
              l.color = 'red';
              break;
            case 'agg':
              l.color = 'black';
              break;

            default:
              l.color = 'black';
              break;
          }
        });


        this.nodes.forEach(n => {
          const node = this.data.filter(r => r.source === n.name)[0];
          n.category = node?.category;
          n.metadata = node?.metadata ? JSON.parse(node?.metadata) : '';
        });


        this.links = this.data.map((n) => ({ source: n.source, target: n.target, value: 10 }));
      }
    });
  }
}
