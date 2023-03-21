import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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

@Component({
  selector: 'app-glossary-list',
  templateUrl: './glossary-list.component.html',
  styleUrls: ['./glossary-list.component.scss']
})
export class GlossaryListComponent implements OnInit {
  private unsubscribe: Subject<void> = new Subject();
  glossary: any;
  projectId: number;
  searchTerm: any;
  loading: boolean;
  certificate$: Observable<Certificate>;
  userId: number;

  constructor(
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private governService: GovernService,
    private modalService: NgbModal,
    private snakbar: SnackbarService,
    public datepipe: DatePipe) {

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
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.governService.getGlossaryList(this.projectId, this.userId).subscribe((response) => {
      this.loading = false;
      this.glossary = response.result.sort((a, b) => b.id - a.id);
      this.glossary.map(t => {
        t.creationDate = this.datepipe.transform(t.creationDate, 'dd-MM-yy, hh:mm a');
        t.modifiedDate = this.datepipe.transform(t.modifiedDate, 'dd-MM-yy, hh:mm a');
      });
    }, () => {
      this.loading = false;
    });
  }

  open(data: any) {
    const modalRef = this.modalService.open(EditComponent, { size: 'lg', windowClass: 'modal-size' });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.id = data.id;
    modalRef.componentInstance.projectId = data.projectId;

    modalRef.result.then((result) => {
      this.loadData();
      console.log(result);
    }, (reason) => {
      console.log(reason);
    });
  }

  onAddPopup() {
    const modalRef = this.modalService.open(BusinessGlossaryComponent, { size: 'lg' });
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.projectId = this.projectId;

    modalRef.result.then((result) => {
      this.loadData();
    }, (reason) => {
      console.log(reason);
    });
  }

  delete(id: number, term_name: string) {
    this.governService.delete(this.userId, id).subscribe((response) => {
      this.snakbar.open(response.message);
      // this.snakbar.open(term_name + " " + "deleted successfully")
      if (response.code == 204) {
        this.loadData();
      }
    }, (reason) => {
      console.log(reason);
    });
  }
}
