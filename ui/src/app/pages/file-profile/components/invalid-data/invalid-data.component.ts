import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-invalid-data',
  templateUrl: './invalid-data.component.html',
  styleUrls: ['./invalid-data.component.scss']
})
export class InvalidDataComponent implements OnInit {

  @Input() projectId: number;
  @Input() userId: number;
  @Input() folderId: number;
  @Input() fileId: number;
  @Input() primarykey: string;

  data: any;
  columns: any;
  loading: boolean;

  constructor(
    private profileService: ProfileService,
    public modal: NgbActiveModal) { }

  ngOnInit(): void {
    this.loading = true;
    this.profileService.getInvalidData(this.projectId, this.userId, this.folderId, this.fileId, this.primarykey).subscribe((response) => {
      this.loading = false;
      // temporary fix as per dicussion with Prakash.
      const data = response?.analytics?.replace(/NaN/g, '0.0');

      this.data = JSON.parse(data);
      if (this.data && this.data.length) {
        this.columns = Object.keys(this.data[0]);
        this.data = this.data[0][this.columns];
      }
    }, (error) => {
      this.loading = false;
    });
  }
}
