import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { CommonRegexPatterns, Dictionary } from '../../models/dictionary';
import { DataDictionaryService } from '../../services/data-dictionary.service';

@Component({
  selector: 'app-dictionary-rule',
  templateUrl: './dictionary-rule.component.html',
  styleUrls: ['./dictionary-rule.component.scss']
})
export class DictionaryRuleComponent implements OnInit {

  private certificate$: Observable<Certificate>;
  private unsubscribe: Subject<void> = new Subject();

  @Input() projectId: number;
  @Input() folderId: number;
  @Input() fileId: number;
  @Input() dictionary: Dictionary;
  @Input() onCreate: boolean;

  public subUsers: any = [];
  public columns: any = [];
  public commonRegexPatterns: Array<CommonRegexPatterns>;
  public fg: FormGroup;
  public userId: number;
  public loading: boolean;
  public isPredefined = true;
  public regexCode: string;
  public selectedColumnName: string;
  public selectedDataType: string;
  public selectedRegularExpression: string;
  public example: any = 123;

  dataTypes = [
    { text: 'String', value: 'string' },
    { text: 'Integer', value: 'int' },
    { text: 'Float', value: 'float' },
    { text: 'decimal', value: 'decimal' },
  ];

  constructor(
    private fb: FormBuilder,
    private snakbar: SnackbarService,
    public modal: NgbActiveModal,
    private quantumFacade: Quantumfacade,
    private dataDictionaryService: DataDictionaryService) { }

  ngOnInit(): void {

    const tags = this.dictionary?.tags?.split(',').map(t => {
      return {
        display: t,
        value: t
      };
    });

    const dataCustodian = this.dictionary?.dataCustodian?.split(',').map(t => {
      return {
        display: t,
        value: t
      };
    });

    this.fg = this.fb.group({
      columnName: new FormControl(this.dictionary?.columnName, Validators.required),
      dataType: new FormControl(this.dictionary?.dataType, Validators.required),
      description: new FormControl(this.dictionary?.description, Validators.required),
      regularExpression: new FormControl(this.dictionary?.regularExpression),
      pdRegexCode: new FormControl(this.dictionary?.pdRegexCode, Validators.required),
      example: new FormControl(this.dictionary?.example, Validators.required),
      dataCustodian: new FormControl(dataCustodian, Validators.required),
      tags: new FormControl(tags, Validators.required),
      regexType: new FormControl(this.dictionary?.regexType),
    });

    if (!this.fg.controls.regexType.value) {
      this.fg.controls.regexType.setValue('PD');
    }

    this.isPredefined = this.fg.controls.regexType.value === 'PD';

    this.certificate$ = this.quantumFacade.certificate$;
    this.certificate$
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(certificate => {
        this.userId = parseInt(certificate.user_id, 10);
      });

    this.getFileData();
  }

  validateRegx(): boolean {
    const matchResults = new RegExp(this.fg.value.regularExpression).test(this.fg.value.example);
    return matchResults ?? false;
  }

  save(isPublished: boolean): void {

    if (this.fg.invalid) {
      this.fg.markAllAsTouched();
      return;
    }

    // Remove validation for time being
    // if (!this.validateRegx()) {
    //   // Invalid Regular expression with example.
    //   this.snakbar.open('Invalid Regular expression with example');
    //   return;
    // }

    this.loading = true;

    const dictionary: Dictionary = {
      projectId: this.projectId,
      fileId: this.fileId,
      folderId: this.folderId,
      columnName: this.fg.controls.columnName.value,
      dataType: this.fg.controls.dataType.value,
      description: this.fg.controls.description.value,
      regularExpression: this.fg.controls.regularExpression.value,
      pdRegexCode: this.fg.controls.pdRegexCode.value,
      example: this.fg.controls.example.value,
      regexType: this.fg.controls.regexType.value,
      dataCustodian: this.fg.controls.dataCustodian.value.map(t => t.display).join(', '),
      tags: this.fg.controls.tags.value.map(t => t.display).join(', '),
      published: isPublished,
    };

    if (this.onCreate) {
      this.dataDictionaryService.save(this.projectId, this.userId, dictionary).subscribe((res) => {
        this.loading = false;
        this.modal.close();
        this.snakbar.open(res.message);
      }, (error) => {
        this.loading = false;
        this.snakbar.open(error);
      });
    } else {
      this.dataDictionaryService.update(this.projectId, this.userId, this.dictionary.id, dictionary).subscribe((res) => {
        this.loading = false;
        this.modal.close();
        this.snakbar.open(res.message);
      }, (error) => {
        this.loading = false;
        this.snakbar.open(error);
      });
    }
    this.getFileData();
  }

  public onItemChange(value: string): void {
    this.fg.controls.regularExpression.clearValidators();
    this.fg.controls.pdRegexCode.clearValidators();

    this.fg.controls.pdRegexCode.reset();
    this.fg.controls.regularExpression.reset();
    this.fg.controls.example.reset();

    if (this.fg.controls.regexType.value === 'PD') {
      this.fg.controls.pdRegexCode.setValidators(Validators.required);
    } else {
      this.fg.controls.regularExpression.setValidators(Validators.required);
    }

    this.fg.controls.regularExpression?.updateValueAndValidity();
    this.fg.controls.pdRegexCode?.updateValueAndValidity();

    this.isPredefined = (value === 'PD') ? true : false;
  }

  getFileData() {
    this.loading = true;
    this.dataDictionaryService.getFileData(this.userId, this.projectId, this.folderId, this.fileId).subscribe((res) => {
      if (res?.code === 200) {
        const dataCustodian = this.dictionary?.dataCustodian?.split(',').map(t => {
          return t.trim();
        });

        this.columns = JSON.parse(res?.result?.fileMetadata);
        this.commonRegexPatterns = res?.result?.commonRegexPatterns;
        const subUsers = res?.result?.subUsers;

        subUsers.map(t => {
          const display = `${t.firstName} ${t.lastName}`;

          if (dataCustodian?.includes(display)) {
            return;
          }

          this.subUsers.push({
            display: `${t.firstName} ${t.lastName}`,
            value: `${t.userId}`
          });
        });
      }
      this.loading = false;
    }, (error) => {
      this.loading = false;
    });
  }

  public onSelectPDRegexCode(pdRegexCode: number): void {
    const v = this.commonRegexPatterns.find(x => x.regexPatternId  === +pdRegexCode);
    if(v.regexName  == "Line starts with numbe"){
      this.example = 123;
    }
    if(v.regexName == "Line starts with character (upper case)"){
      this.example = "Abcd";
    }
    if(v.regexName == "Line starts with character (lower case)"){
      this.example = "abcd";
    }
    if(v.regexName == "Line starts with character (case-insensitive)"){
      this.example = "aBcD";
    }
    if(v.regexName == "Line starts with special character"){
      this.example = "$abc";
    }
    if (v) {
      this.fg.controls.regularExpression.setValue(v.regexCode);
    }
  }
}
