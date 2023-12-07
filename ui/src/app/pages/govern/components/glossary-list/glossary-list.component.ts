import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GovernService } from '../../services/govern.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { EditComponent } from '../edit/edit.component';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { BusinessGlossaryComponent } from '../business-glossary/business-glossary.component';
import { Observable, Subject } from 'rxjs';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { takeUntil } from 'rxjs/operators';
import { DatePipe } from '@angular/common';
import { Pager } from '../../models/pager';
import { FolderService } from 'src/app/pages/file-profile/services/folder.service';
import { FilterPipe } from 'ngx-filter-pipe';


@Component({
  selector: 'app-glossary-list',
  templateUrl: './glossary-list.component.html',
  styleUrls: ['./glossary-list.component.scss']
})
export class GlossaryListComponent implements OnInit {
  private unsubscribe: Subject<void> = new Subject();
  glossary: any;
  originalGlossary: any;
  projectId: number;
  searchTerm: any;
  loading: boolean;
  certificate$: Observable<Certificate>;
  userId: number;
  public isDescending: boolean;
  public hasGlossary: boolean;
  public searchDiv: boolean = false;
  public searchString: string;
  public projectName: string;

  public startIndex: number = 0;
  public pageSize: number = 10;
  public endIndex: number = this.pageSize;
  public currnetPage: number = 0;
  public totalPages: number;
  public pager: Array<Pager> = [];
  public hasFolders: boolean;

  constructor(
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private governService: GovernService,
    private modalService: NgbModal,
    private snakbar: SnackbarService,
    public datepipe: DatePipe,
    private router: Router,
    private folderService: FolderService,
    private filterPipe: FilterPipe) {

    this.projectName = localStorage.getItem('projectname');

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((certificate: Certificate) => {
        this.userId = +certificate.user_id;
      });

    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'));
    this.activatedRoute.queryParams.subscribe(params => {
      if (+params.isNew) {
        this.onAddPopup();
      }
    });
  }

  ngOnInit(): void {
    this.foldersList();
  }

  foldersList() {
    this.loading = true;
    this.folderService.getFolders(this.projectId, this.userId).subscribe((response: any) => {
      this.hasFolders = response.result?.length;
      console.log(response.result?.length);
      this.loading = false;
      if (this.hasFolders) {
        // this.getProjectStats();
        this.loadData();
      } else {

      }
    }, () => {
      this.hasFolders = false;
    });
  }

  loadData(): void {
    this.loading = true;
    this.governService.getGlossaryList(this.projectId, this.userId).subscribe((response) => {
      this.loading = false;
      this.hasGlossary = response.result?.length;
      this.glossary = response.result.sort((a, b) => b.id - a.id);
      this.glossary.map(t => {
        t.creationDate = this.datepipe.transform(t.creationDate, 'dd-MM-yy, hh:mm a');
        t.modifiedDate = this.datepipe.transform(t.modifiedDate, 'dd-MM-yy, hh:mm a');
      });

      this.totalPages = this.glossary.length / this.pageSize;
      this.totalPages = Math.ceil(this.totalPages);

      for (let index = 0; index < this.totalPages; index++) {
        const page = {
          index,
          isActive: index == 0
        } as Pager;

        this.pager.push(page);
      }

      this.originalGlossary = [...this.glossary];
    }, () => {
      this.loading = false;
      this.hasGlossary = false;
    });
  }

  open(data: any) {
    this.router.navigate([`projects/${this.projectId}/govern/dashboard/list/update-glosssary`], {
      queryParams: {
        dataId: data.id
      }
    });
  }

  view(data: any) {
    this.router.navigate([`projects/${this.projectId}/govern/dashboard/schart`], {
      queryParams: {
        dataId: data.id
      }
    });
  }

  public onAddPopup(): void {
    this.router.navigate([`projects/${this.projectId}/govern/dashboard/list/glossary`]);
  }

  // delete(id: number, term_name: string) {
  //   this.governService.delete(this.userId, id).subscribe((response) => {
  //     this.snakbar.open(response.message);
  //     // this.snakbar.open(term_name + " " + "deleted successfully")
  //     if (response.code == 204) {
  //       this.loadData();
  //     }
  //   }, (reason) => {
  //     console.log(reason);
  //   });
  // }

  // public openDeleteGlossary(id: number, term_name: string): void {
  //   const modalRef = this.modalService.open(DeleteGlossaryComponent, { size: 'md modal-dialog-centered', scrollable: false });
  //   modalRef.componentInstance.userId = this.userId;
  //   modalRef.componentInstance.id = id;
  //   modalRef.result.then((result) => {
  //     this.pager = [];
  //     this.loadData();
  //   }, (reason) => {
  //     console.log(reason);
  //     this.pager = [];
  //     this.loadData();
  //   });
  // }

  public sortTerm(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.glossary = this.glossary.sort((a, b) => {
        var term_order = a.term.localeCompare(b.term);
        return term_order;
      });
    } else {
      this.glossary = this.glossary.sort((a, b) => {
        var term_order = b.term.localeCompare(a.term);
        return term_order;
      });
    }
  }

  public sortStatus(): void {
    this.isDescending = !this.isDescending;

    if (this.isDescending) {
      this.glossary = this.glossary.sort((a, b) => b.published - a.published);
    } else {
      this.glossary = this.glossary.sort((a, b) => a.published - b.published);
    }
  }

  searchInput(str) {
    this.searchString = str;
    if (str.length == 0) {
      this.searchDiv = false;
    } else {
      this.searchDiv = true;
      this.glossary = this.filterPipe.transform(this.originalGlossary, { term: this.searchString });
      this.startIndex = 0;
    }
  }

  clearSearhInput() {
    this.searchTerm = '';
    this.searchDiv = false;
    this.glossary = this.originalGlossary;
    this.startIndex = 0;
    this.endIndex = this.pageSize;
  }

  redirectToCreate(): void {
    this.router.navigate([`projects/${this.projectId}/govern/dashboard/list/glossary`]);
  }

  public previousPage(): void {
    this.currnetPage--;
    this.pager.map(t => t.isActive = false);

    if (this.currnetPage == 0) {
      this.currnetPage = 0;
      this.startIndex = 0;
      this.endIndex = this.pageSize;
    } else {
      this.startIndex = this.pageSize * this.currnetPage;
      this.endIndex = this.startIndex + this.pageSize;
    }

    this.pager[this.currnetPage].isActive = true;
  }

  public nextPage(): void {
    this.pager.map(t => t.isActive = false);

    this.currnetPage++;

    if (this.totalPages != this.currnetPage) {
      this.startIndex = this.endIndex;
      this.endIndex = this.startIndex + this.pageSize;
    }

    this.pager[this.currnetPage].isActive = true;
  }

  public redirectToPageIndex(pager: Pager): void {
    this.pager.map(t => t.isActive = false);
    pager.isActive = true;
    this.currnetPage = pager.index;
    this.startIndex = this.pageSize * this.currnetPage;
    this.endIndex = this.startIndex + this.pageSize;
  }
}
