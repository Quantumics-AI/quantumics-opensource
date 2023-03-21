import { Component, Input, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { GovernService } from '../../services/govern.service';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-business-glossary',
  templateUrl: './business-glossary.component.html',
  styleUrls: ['./business-glossary.component.scss']
})
export class BusinessGlossaryComponent implements OnInit {
  @Input() userId: number;
  @Input() projectId: number;
  fg: FormGroup;
  columnInfo = [];
  data: any = {};
  lengthMsg: boolean = false;
  lengthDesMsg: boolean = false;
  lengthDataMsg: boolean = false;
  lengthBRMsg: boolean = false;
  lengthAcrMsg: boolean = false;
  lengthSTMsg: boolean = false;

  constructor(
    private fb: FormBuilder,
    private governService: GovernService,
    private router: Router,
    private snakbar: SnackbarService,
    public modal: NgbActiveModal
  ) {
  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      term: new FormControl('', Validators.required),
      description: new FormControl('', Validators.required),
      dataOwner: new FormControl('', Validators.required),
      businessRules: new FormControl('', [Validators.required, Validators.pattern('^[a-zA-Z ]+')]),
      acronym: new FormControl('', Validators.required),
      synonymTerm: new FormControl('', Validators.required),
      date: new FormControl(''),
      relatedAttributes: new FormControl('', Validators.required),
      addTags: new FormControl('', Validators.required),
      projectId: new FormControl(this.projectId),
      published: new FormControl(''),
    });

    this.governService.getColumnInfo(this.projectId, this.userId).subscribe((res) => {
      this.columnInfo = res.result;
    }, () => {
      this.columnInfo = [];
    });
  }

  save(isPublish: boolean): void {

    if (!this.fg.valid) {
      this.fg.markAllAsTouched();
      return;
    }

    const payload = {
      term: this.fg.value.term,
      definition: this.fg.value.description,
      dataOwner: this.fg.value.dataOwner,
      businessRules: this.fg.value.businessRules,
      acronym: this.fg.value.acronym,
      synonymTerm: this.fg.value.synonymTerm,
      date: this.fg.value.date,
      relatedAttributes: this.fg.value.relatedAttributes?.map(t => t).join(', '),
      addTags: this.fg.value.addTags?.map(t => t.display).join(','),
      projectId: this.projectId,
      published: isPublish,
    };

    // if(isPublish){
    //   this.governService.create(this.userId, payload).subscribe((response) => {
    //     // response.message
    //     this.snakbar.open(this.fg.value.term + " " + "business glossary published successfully.");
    //     this.modal.close();
    //     this.router.navigate([`projects/${this.projectId}/govern/dashboard/list`]);
    //   }, () => {
    //     this.snakbar.open('Error');
    //   });
    // } else {
    //   this.governService.create(this.userId, payload).subscribe((response) => {
    //     this.snakbar.open(this.fg.value.term + " " + "business glossary saved successfully");
    //     this.modal.close();
    //     this.router.navigate([`projects/${this.projectId}/govern/dashboard/list`]);
    //   }, () => {
    //     this.snakbar.open('Error');
    //   });
    //   console.log()
    // }

    this.governService.create(this.userId, payload).subscribe((response) => {
      this.snakbar.open(response.message);
      this.modal.close();
      this.router.navigate([`projects/${this.projectId}/govern/dashboard/list`]);
    }, () => {
      this.snakbar.open('Error');
    });

    
  }

  modelChangeTerm(newObj) {

    if (newObj.length == 20) {
      this.lengthMsg = true
      setTimeout(() => {
        this.lengthMsg = false
      }, 5000);
      
    } else {
      this.lengthMsg = false
    }
  }

  modelChangeDescripton(newObj) {

    if (newObj.length == 50) {
      this.lengthDesMsg = true
      setTimeout(() => { 
        this.lengthDesMsg = false
       }, 5000);
    } else {
      this.lengthDesMsg = false
    }
  }

  modelChangeDO(newObj) {

    if (newObj.length == 20) {
      this.lengthDataMsg = true
      setTimeout(() => {
        this.lengthDataMsg = false
      }, 5000);
    } else {
      this.lengthDataMsg = false
    }
  }

  modelChangeBR(newObj) {

    if (newObj.length == 20) {
      this.lengthBRMsg = true
      setTimeout(() => {
        this.lengthBRMsg = false
      }, 5000);
    } else {
      this.lengthBRMsg = false
    }
  }

  modelChangeAcronym(newObj) {

    if (newObj.length == 20) {
      this.lengthAcrMsg = true
      setTimeout(() => {
        this.lengthAcrMsg = false
      }, 5000);
    } else {
      this.lengthAcrMsg = false
    }
  }

  modelChangeST(newObj) {

    if (newObj.length == 20) {
      this.lengthSTMsg = true
      setTimeout(() => {
        this.lengthSTMsg = false
      }, 5000);
    } else {
      this.lengthSTMsg = false
    }
  }
}
