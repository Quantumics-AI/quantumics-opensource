import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-add-folder',
  templateUrl: './add-folder.component.html',
  styleUrls: ['./add-folder.component.scss']
})
export class AddFolderComponent implements OnInit {

  @Output() create: EventEmitter<any> = new EventEmitter<any>();
  fg: FormGroup;

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
    this.fg = this.fb.group({
      folderName: ['', [Validators.required, Validators.maxLength(30)]],
      folderDesc: ['', Validators.maxLength(255)],
    });
  }

  save(): void {
    this.create.emit(this.fg.value);
    this.fg.reset();
  }

}
