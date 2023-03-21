import { Component, OnInit } from '@angular/core';
import {ProjectsService} from '../../services/projects.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { ActivatedRoute } from '@angular/router';
import { takeUntil } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import {UdfData} from '../../models/project';
import {UpdateUdfComponent} from '../update-udf/update-udf.component'

@Component({
  selector: 'app-edit-udf',
  templateUrl: './edit-udf.component.html',
  styleUrls: ['./edit-udf.component.scss']
})
export class EditUdfComponent implements OnInit {
  udfList: any[];
  userId: number;
  projectId: number;

  public loading: boolean;

  public searchUdf : any;

  private unsubscribe: Subject<void> = new Subject<void>();

  constructor(
    private projectsService: ProjectsService,
    private quantumFacade: Quantumfacade,
    private activatedRoute: ActivatedRoute,
    private snakbar: SnackbarService,
    private modalService: NgbModal,
  ) {
    this.quantumFacade.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    this.projectId = parseInt(this.activatedRoute.parent.snapshot.paramMap.get('projectId'), 10);
   }

  ngOnInit(): void {
    this.getUdf();
  }

  private getUdf(): void {
    this.loading = true;
    this.projectsService.getUdf(this.projectId,this.userId).subscribe((res) => {
      this.loading = false;
      if(res?.code === 200){
        this.udfList = res?.result;
      }
      
    }, () => {
      this.loading = false;
    });
  }

  deleteUdf(d: any): void {
    if(confirm("Are you sure to delete "+d.udfName)) {
      this.projectsService.deleteUdfData(d.projectId, d.userId, d.udfId).subscribe((res) => {
        this.snakbar.open(res.message);
        this.getUdf();
      });
    }
    
  }

  onEditUDF(udfData : UdfData){
    const modalRef = this.modalService.open(UpdateUdfComponent, {
      size: 'xl',
      windowClass: 'modal-size'
      
    });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.udfData = udfData;

    modalRef.result.then((result) => {
      this.getUdf();
    }, (reason) => {
      console.log(reason);
    });
    
  }

}
