import { Component, OnInit, Input } from '@angular/core';
import { CleansingDataService } from '../../services/cleansing-data.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-list-cleansed-files',
  templateUrl: './list-cleansed-files.component.html',
  styleUrls: ['./list-cleansed-files.component.scss']
})
export class ListCleansedFilesComponent implements OnInit {
  @Input() projectId: string;
  @Input() userId: number;
  @Input() folderId: number;
  @Input() fileName: string;
  @Input() folderName: string;
  files = [];
  loading: boolean;

  constructor(
    private cleansingDataService: CleansingDataService,
    public modal: NgbActiveModal,
    private activatedRoute: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.loading = true;
    this.cleansingDataService.getAddedCleansingFiles(parseInt(this.projectId, 0), this.userId, this.folderId).subscribe(response => {
      this.loading = false;
      console.log(response);
      if (response?.code === 200) {
        this.files = response.result;
      }
    }, (error) => {
      this.loading = false;
    });
  }

  cleansingEditor(fileId: number, fileName: string): void {
    this.modal.close('closed');

    localStorage.setItem('cleansing-selected-folder-name', this.folderName);
    localStorage.setItem('cleansing-selected-file-name', fileName);
    this.router.navigate([`projects/${this.projectId}/cleansing/${this.folderId}/${fileId}`], {
      relativeTo: this.activatedRoute
    });
  }

}
