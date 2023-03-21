import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { FoldersService } from '../../services/folders.service';

@Component({
  selector: 'app-edit-folder',
  templateUrl: './edit-folder.component.html',
  styleUrls: ['./edit-folder.component.scss']
})
export class EditFolderComponent implements OnInit {
  @Input() projectId: number;
  @Input() userId: number;
  @Input() folder: any;
  @Output() updatedFolder = new EventEmitter<any>();
  @Output() close = new EventEmitter<boolean>();
  fg: FormGroup;

  constructor(
    private fb: FormBuilder,
    private foldersService: FoldersService,
    private snakbar: SnackbarService) { }

  ngOnInit(): void {
    this.fg = this.fb.group({
      folderId: [this.folder.folderId],
      folderName: [this.folder.folderDisplayName ?? this.folder.folderName, [Validators.required, Validators.maxLength(30)]],
      folderDesc: [this.folder.folderDesc, Validators.maxLength(255)],
    });
  }

  public save(): void {
    const request = {
      folderDisplayName: this.fg.value.folderName,
      folderDesc: this.fg.value.folderDesc
    };

    this.foldersService.updateFolder(this.projectId, this.userId, this.folder.folderId, request).subscribe((response) => {
      this.snakbar.open(response.message);
      this.updatedFolder.emit(response?.result);
    }, (error) => {
      this.snakbar.open(error);
    });
  }
}
