import { Component, Input, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { InvalidDataComponent } from '../invalid-data/invalid-data.component';

@Component({
  selector: 'app-quality',
  templateUrl: './quality.component.html',
  styleUrls: ['./quality.component.scss']
})
export class QualityComponent implements OnInit {

  @Input() dataQuality: any;
  @Input() userId: number;
  @Input() projectId: number;
  @Input() folderId: number;
  @Input() fileId: number;

  public searchTerm: string;

  constructor(private modalService: NgbModal) { }

  ngOnInit(): void {
  }

  public getInvalidData(data: any): void {

    if (data?.inValid <= 0) {
      return;
    }

    const modalRef = this.modalService.open(InvalidDataComponent, { size: 'sm' });
    modalRef.componentInstance.projectId = this.projectId;
    modalRef.componentInstance.userId = this.userId;
    modalRef.componentInstance.folderId = this.folderId;
    modalRef.componentInstance.fileId = this.fileId;
    modalRef.componentInstance.primarykey = data?.ddId;
  }

}
