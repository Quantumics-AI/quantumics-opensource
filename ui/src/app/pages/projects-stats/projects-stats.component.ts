import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { FolderService } from '../file-profile/services/folder.service';
import { StatsService } from './services/stats.service';

@Component({
  selector: 'app-projects-stats',
  templateUrl: './projects-stats.component.html',
  styleUrls: ['./projects-stats.component.scss']
})
export class ProjectsStatsComponent implements OnInit {
  public projectId: number;
  public userId: number;
  public requestType: string;
  public stats: any;
  public selectedTopbarOption: string;
  public projectName: string;
  public selectedMannual: any;
  public totalFileData: any;
  public hasFolders: boolean;

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private statsService: StatsService,
    private folderService: FolderService,
    private router: Router) {
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    this.projectName = localStorage.getItem('projectname');
  }

  ngOnInit(): void {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
    this.requestType = localStorage.getItem('request_type');

    this.folderService.getFolders(this.projectId, this.userId).subscribe((response: any) => {
      this.hasFolders = response.result?.length;

      if (this.hasFolders) {
        this.getProjectStats();
      }
    }, () => {
      this.hasFolders = false;
    });
  }


  public selectedOption(type: any): void {
    this.selectedTopbarOption = type;
  }

  public selectedEffort(efforts: any): void {
    this.selectedMannual = efforts;
  }

  public selectedFileData(totalfile: any): void {
    this.totalFileData = totalfile;
  }

  private getProjectStats(): void {
    this.statsService.getProjectStats(this.userId, this.projectId, this.requestType).subscribe(res => {
      this.stats = res.result;
    });
  }
}
