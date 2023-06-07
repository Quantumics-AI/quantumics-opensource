import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { FolderService } from '../file-profile/services/folder.service';
import { StatsService } from './services/stats.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { SampleDatasetComponent } from './sample-dataset/sample-dataset.component';
import { KpiFilter } from './Models/kpi-filter.model';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

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

  public applyKpiFilter: boolean;

  private unsubscribe: Subject<void> = new Subject<void>();
  public apikpiDetails: any;
  // public kpiFilters: Array<KpiFilter> = [];
  public kpiFilters = [

    { id: 1, label: 'Dashboard', name: 'Dashboard', selected: false },
    { id: 2, label: 'Storage', name: 'Storage', selected: false },
    { id: 3, label: 'Dataset', name: 'Dataset', selected: false },
    { id: 4, label: 'Data flow', name: 'DataFlow', selected: false },
    { id: 5, label: 'Prepared Datasets', name: 'PreparedDS', selected: false },
    { id: 6, label: 'Folder', name: 'Folder', selected: false },
    { id: 7, label: 'Pipeline', name: 'Pipeline', selected: false },
  ] as KpiFilter[];

  private tmpKpiFilters: Array<KpiFilter> = [];
  public getKpiList: Array<KpiFilter> = [];

  public sortView: string;
  public applyBtn: boolean = true;
  public selectedKpiData: any;
  loading: boolean;
  public loadingData: boolean;
  public kpiListStore: any;

  constructor(
    private activatedRoute: ActivatedRoute,
    private quantumFacade: Quantumfacade,
    private statsService: StatsService,
    private folderService: FolderService,
    private router: Router,
    private modalService: NgbModal,
    private snackBar: SnackbarService) {
    // localStorage.setItem('kpilist', JSON.stringify(this.kpiFilters));
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    this.sortView = localStorage.getItem('stats');
    // this.getKpiList = JSON.parse(list) as KpiFilter[];
    this.projectName = localStorage.getItem('projectname');
    // this.tmpKpiFilters = JSON.parse(JSON.stringify(this.getKpiList));
  }

  ngOnInit(): void {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
    // this.requestType = localStorage.getItem('request_type');
    this.loading = true;
    this.loadingData = true;
    this.folderService.getFolders(this.projectId, this.userId).subscribe((response: any) => {
      this.hasFolders = response.result?.length;
      this.loading = false;
      this.loadingData = false;
      if (this.hasFolders) {
        const localKpiData = localStorage.getItem('kpilist');
        if (localKpiData == null) {
          this.fetchKpiData();
        } else {
          this.getKpiList = JSON.parse(localKpiData) as KpiFilter[];
          const t = this.getKpiList.filter(item => item.selected === true);
          this.apikpiDetails = t.map(item => item.name);
          this.getProjectStats();
        }
      }
    }, () => {
      this.hasFolders = false;
    });
  }

  public redirectToPlan(): void {
    this.projectId = +this.activatedRoute.snapshot.paramMap.get('projectId');
    this.router.navigate([`projects/${this.projectId}/workspace-setting/plans`]);
  }

  public selectedOption(type: any): void {
    this.selectedTopbarOption = type;
    this.getKpiList.map(t => {
      if (t.label === this.selectedTopbarOption) {
        t.selectedTopbarOption = true;
      } else {
        t.selectedTopbarOption = false;
      }
    });
  }

  public selectedEffort(efforts: any): void {
    this.selectedMannual = efforts;
  }

  public selectedFileData(totalfile: any): void {
    this.totalFileData = totalfile;
  }

  private getProjectStats(): void {
    // this.loading = true;
    this.loadingData = true;
    this.selectedKpiData = this.getKpiList.filter((k) => { return k.selected == true });
    const params = this.selectedKpiData.map(item => item.name);
    // const params =['Dashboard', 'Storage', 'Dataset', 'DataFlow'];
    this.statsService.getProjectStats(this.userId, this.projectId, this.sortView, params).subscribe(res => {
      this.stats = res.result;
      // this.loading = false;
      this.loadingData = false;
      this.totalFileData = res.result;
    }, (reason) => {
      this.loadingData = false;
      this.snackBar.open(reason);
    });
  }

  sampleDataset(): void {
    const modalRef: NgbModalRef = this.modalService.open(SampleDatasetComponent, { size: 'lg' });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.result.then((res) => {
      // this.sharedService.createFlow(res);
    }, (reason) => {
      console.log(reason);
    });
  }

  public changeKpiFilter(event: any, filter: KpiFilter): void {
    if (event.target.checked) {
      // Check if the user has already selected four checkboxes
      const noOfSelectedKPI = this.getKpiList.filter((opt) => opt.selected).length;
      if (noOfSelectedKPI <= 3) {
        this.applyBtn = true;
      }

      if (noOfSelectedKPI >= 4) {
        // If the user has already selected four checkboxes, prevent the selection
        event.target.checked = false;
      } else {
        filter.selected = event.target.checked;
      }
    } else {
      filter.selected = event.target.checked;
      const noOfSelectedKPI = this.getKpiList.filter((opt) => opt.selected).length;
      if (noOfSelectedKPI >= 1) {
        this.applyBtn = true;
      } else {
        this.applyBtn = false;
      }
    }

    // if (event.currentTarget.checked == true) {
    //   this.kpiFilters.find(x => x.name == filter.name).selected = true;
    // } else {
    //   this.kpiFilters.find(x => x.name == filter.name).selected = false;
    // }

  }

  public sortViewData(s: string): void {
    if (s == 'w') {
      this.sortView = s;
      localStorage.setItem('stats', s);
      this.getProjectStats();
    } else if (s == 'm') {
      this.sortView = s;
      localStorage.setItem('stats', s);
      this.getProjectStats();
    } else if (s == 'y') {
      this.sortView = s;
      localStorage.setItem('stats', s);
      this.getProjectStats();
    }

  }

  public addKpi(popover: any): void {
    this.loadingData = true;
    this.applyKpiFilter = !this.applyKpiFilter;
    localStorage.setItem('kpilist', JSON.stringify(this.getKpiList));
    this.tmpKpiFilters = JSON.parse(JSON.stringify(this.getKpiList));

    popover.close();
    this.updateKpi();
  }

  public onPopoverHidden(): void {
    // this.getKpiList = JSON.parse(JSON.stringify(this.tmpKpiFilters));
    this.getKpiList.map(t => {
      if (this.apikpiDetails.indexOf(t.name) > -1) {
        t.selected = true;
      } else {
        t.selected = false
      }
    });
  }

  public fetchKpiData(): void {
    this.statsService.getKpiData(this.userId, this.projectId).subscribe(res => {
      // get result
      if (res.code === 200) {
        this.apikpiDetails = res.result.kpiDetails.split(',');
        this.kpiFilters.map(t => {
          if (this.apikpiDetails.indexOf(t.name) > -1) {
            t.selected = true;
          } else {
            t.selected = false
          }
        });
        localStorage.setItem('kpilist', JSON.stringify(this.kpiFilters));
        const list = localStorage.getItem('kpilist');
        this.getKpiList = JSON.parse(list) as KpiFilter[];
        this.tmpKpiFilters = JSON.parse(JSON.stringify(this.getKpiList));

      }
      this.getProjectStats();

    }, () => {

    });
  }

  public updateKpi(): void {
    const t = this.tmpKpiFilters.filter(item => item.selected === true);
    var kpiSelectedDetails = t.map(item => item.name);

    const request = {
      "projectId": this.projectId,
      "userId": this.userId,
      "kpiDetails": kpiSelectedDetails.join(',')
    };

    this.statsService.updateKpi(request).subscribe((res) => {
      // update data
      this.fetchKpiData();
      this.snackBar.open(res.message);
    }, (reason) => {
      this.snackBar.open(reason)
    });
  }

}
