import { Component, OnInit, EventEmitter, Output } from '@angular/core';
import { StatsService } from '../../projects-stats/services/stats.service';
import { ActivatedRoute } from '@angular/router';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-right-side',
  templateUrl: './right-side.component.html',
  styleUrls: ['./right-side.component.scss']
})
export class RightSideComponent implements OnInit {

  @Output() selectedEValue = new EventEmitter<any>();
  @Output() selectedFData = new EventEmitter<any>();

  public selectedIndex: number;
  projectId: number;
  userId: number;
  requestType: string;
  stats: any;
  time: number;
  money: number;

  public perios = [
    {
      label: 'Week',
      value: 'w'
    },
    {
      label: 'Month',
      value: 'm'
    },
    {
      label: 'Year',
      value: 'y'
    }
  ];
  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(private activatedRoute: ActivatedRoute, 
    private quantumFacade: Quantumfacade,
    private statsService: StatsService) { 

      this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });
    }

  ngOnInit(): void {
    this.requestType = 'w'
    localStorage.setItem('request_type', this.requestType);
    this.selectedIndex = 0;
    this.getProjectStatsData();
  }

  public getProjectStatsData(): void {
    this.projectId = parseInt(this.activatedRoute.snapshot.paramMap.get('projectId'), 10);
    this.statsService.getProjectStats(this.userId, this.projectId, this.requestType).subscribe(res => {
      this.stats = res.result;
      this.time = res.result.savings.time.value;
      this.money = res.result.savings.money.value;
      this.selectedEValue.emit(this.stats.savings.effort);
      this.selectedFData.emit(this.stats);
      // console.log(res.result.sourceDataset.total)
    });
  }

  public selectPeriod(index: number, data: string): void {
    this.selectedIndex = index;
    localStorage.setItem('request_type', data);


    this.requestType = localStorage.getItem('request_type');
    this.getProjectStatsData();
    this.selectedEValue.emit(this.stats.savings.effort);
    this.selectedFData.emit(this.stats);
  }
}
