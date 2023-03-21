import { Component, Input, OnInit, AfterViewInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { GovernService } from '../../services/govern.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';

@Component({
  selector: 'app-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.scss']
})
export class EditComponent implements OnInit, AfterViewInit {
  @Input() userId: number;
  @Input() id: number;
  @Input() projectId: number;

  fg: FormGroup;
  data: any = {};
  columnInfo = [];
  loading = true;

  constructor(
    public modal: NgbActiveModal,
    private fb: FormBuilder,
    private governService: GovernService,
    private snakbar: SnackbarService) {
  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      term: new FormControl('', Validators.required),
      definition: new FormControl('', Validators.required),
      dataOwner: new FormControl('', Validators.required),
      businessRules: new FormControl('', [Validators.required, Validators.pattern('^[a-zA-Z ]+')]),
      acronym: new FormControl('', Validators.required),
      synonymTerm: new FormControl('', Validators.required),
      date: new FormControl(''),
      relatedAttributes: new FormControl('', Validators.required),
      addTags: new FormControl('', Validators.required),
      projectId: new FormControl(this.projectId),
      id: new FormControl(this.id),
      published: new FormControl('')
    });

    this.governService.getColumnInfo(this.projectId, this.userId).subscribe((res) => {
      this.columnInfo = res.result;
    }, () => {
      this.columnInfo = [];
    });
  }

  ngAfterViewInit(): void {
    this.getData();
  }

  getData(): void {
    this.loading = true;
    this.governService.get(this.projectId, this.userId, this.id).subscribe((response) => {
      this.loading = false;
      if (!response) {
        return;
      }

      const result = response.result;
      this.fg.controls.term.setValue(result?.term);
      this.fg.controls.definition.setValue(result?.definition);
      this.fg.controls.dataOwner.setValue(result?.dataOwner);
      this.fg.controls.businessRules.setValue(result?.businessRules);
      this.fg.controls.acronym.setValue(result?.acronym);
      this.fg.controls.synonymTerm.setValue(result?.synonymTerm);
      this.fg.controls.relatedAttributes.setValue(result?.relatedAttributes?.split(',')?.map(t => {
        return t;
      }));
      this.fg.controls.addTags.setValue(result?.addTags?.split(',').map(t => {
        return {
          value: t,
          display: t
        };
      }));

    }, () => {
      this.loading = false;
      this.snakbar.open('Error');
    });
  }

  update(isPublish: boolean): void {
    if (this.fg.invalid) {
      return;
    }

    this.loading = true;

    const payload = {
      id: this.id,
      term: this.fg.value.term,
      definition: this.fg.value.definition,
      dataOwner: this.fg.value.dataOwner,
      businessRules: this.fg.value.businessRules,
      acronym: this.fg.value.acronym,
      synonymTerm: this.fg.value.synonymTerm,
      published: isPublish,
      date: null,
      addTags: this.fg.value.addTags?.map(t => t.display).join(','),
      relatedAttributes: this.fg.value.relatedAttributes?.map(t => t).join(', ')
    };


    this.governService.update(this.userId, payload).subscribe((response) => {
      this.loading = false;
      this.modal.close();
      this.snakbar.open(response.message);
    }, () => {
      this.loading = false;
      this.modal.close();
      this.snakbar.open('Error');
    });
  }
}

