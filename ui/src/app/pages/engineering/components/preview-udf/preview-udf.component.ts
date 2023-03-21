import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { JoinTypes, PreviewContent, SelectedUdf } from '../../models/configuration';
import { PreviewComponent } from '../preview/preview.component';

@Component({
  selector: 'app-preview-udf',
  templateUrl: './preview-udf.component.html',
  styleUrls: ['./preview-udf.component.scss']
})
export class PreviewUdfComponent implements OnInit {

  @Input() selectedUdf: SelectedUdf;
  @Output() saveUdfData = new EventEmitter<any>();
  @Output() updateJoinImage = new EventEmitter<any>();
  @Output() closePreviewPanel = new EventEmitter<boolean>();

  public fg: FormGroup;
  public fgParameter: FormGroup;
  public files = [];
  public columns = [];
  public UdfParameter: UdfParameter[] = [];
  public joinTypes: JoinTypes;
  public previewHover: boolean;
  public activeTab: string;
  public dataFrame = [];
  public selectVaribale = [];
  public storedVariable = [];
  public createdUdfColumnData: any;
  public showArguments: any;
  private udfParams: string = '';
  private udfName: string = '';

  constructor(
    private fb: FormBuilder,
    private modalService: NgbModal,
    private snackbar: SnackbarService) {
    this.joinTypes = new JoinTypes();
  }

  ngOnInit(): void {
    this.fg = this.fb.group({
      firstFileColumn: new FormControl(''),
      secondFileColumn: new FormControl(''),
      object_0: new FormControl(''),
      object_1: new FormControl(''),
      object_2: new FormControl(''),
      object_3: new FormControl(''),
      object_4: new FormControl(''),
      udfName: new FormControl(this.selectedUdf?.joinName, Validators.required),
      udfsyntax: new FormControl(this.selectedUdf?.udfFunction, Validators.required),
      variable_0: new FormControl(this.selectedUdf?.variable[0]?.variable,),
      variable_1: new FormControl(this.selectedUdf?.variable[1]?.variable,),
      variable_2: new FormControl(this.selectedUdf?.variable[2]?.variable,),
      variable_3: new FormControl(this.selectedUdf?.variable[3]?.variable,),
      variable_4: new FormControl(this.selectedUdf?.variable[4]?.variable,),
    });

    this.fgParameter = this.fb.group({
      dataFrame: new FormControl('', Validators.required),
      parameter: new FormControl('', Validators.required),
      variable: new FormControl(''),
      udfParameterBuilder: new FormControl(this.selectedUdf.joinOperations)
    });

    this.showArguments = this.selectedUdf.arguments.find(o => o.dataValue.toLowerCase() === 'argument');

    this.selectedUdf.arguments.map(t => {
      if (t.dataValue.toLowerCase() === 'data frame') {
        this.dataFrame.push(t.dataValue);
      } else if (t.dataValue.toLowerCase() === 'variable') {
        this.selectVaribale.push(t.dataValue);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.activeTab = 'lnkUdf';
    this.files = this.selectedUdf?.joinFolders;

    setTimeout(() => {
      this.fg.controls.udfName.setValue(this.selectedUdf?.joinName);
      this.fg.controls.firstFileColumn.setValue(this.selectedUdf?.firstFileColumn);
      this.fg.controls.secondFileColumn.setValue(this.selectedUdf?.secondFileColumn);
      this.fg.controls.udfsyntax.setValue(this.selectedUdf?.udfFunction);
      this.fg.controls.object_0.setValue(this.files[0]?.file);
      this.fg.controls.object_1.setValue(this.files[1]?.file);
      this.fg.controls.object_2.setValue(this.files[2]?.file);
      this.fg.controls.object_3.setValue(this.files[3]?.file);
      this.fg.controls.object_4.setValue(this.files[4]?.file);
    }, 10);
  }

  save() {

    if (this.fg.invalid) {
      return;
    }

    for (let index = 0; index < 5; index++) {
      const hasVariableValue = eval(`this.fg.value.variable_${index}`);
      if (hasVariableValue) {
        this.storedVariable.push({
          variable: hasVariableValue
        });
      }
    }

    this.selectedUdf.variable = this.storedVariable;
    this.udfParams = this.udfParams.slice(0, -1);

    const functionBuilder = `${this.udfName}(${this.udfParams})`;
    this.fgParameter.controls.udfParameterBuilder.setValue(functionBuilder.slice(0, -1));

    this.saveUdfData.emit({
      eventIds: this.selectedUdf?.eventIds,
      udfName: this.fg.value.udfName,
      udfFunction: `${this.udfName}(${this.udfParams})`,
      firstFileColumn: this.fg.value.firstFileColumn,
      secondFileColumn: this.fg.value.secondFileColumn,
      udfId: this.selectedUdf?.fileId,
      file_type: this.selectedUdf?.fileType,
      variable: this.selectedUdf.variable,
      arguments: this.selectedUdf.arguments,
    });

    this.selectedUdf.content = new PreviewContent();
    this.selectedUdf.content.disabledPreview = true;
    this.selectedUdf.content.isDisabledApply = true;
  }

  public onSelectJoin(): void {
    this.updateJoinImage.emit({
      joinType: this.fg.value.joinType,
      eventIds: this.selectedUdf?.eventIds
    });
  }

  public viewFullPreviewData(): void {

    if (!this.selectedUdf?.content?.rowData?.length) {
      return;
    }

    if (!this.selectedUdf?.content?.rowColumns?.length) {
      return;
    }

    const modalRef = this.modalService.open(PreviewComponent,
      { size: 'lg', backdrop: 'static' }
    );
    modalRef.componentInstance.data = this.selectedUdf.content.rowData;
    modalRef.componentInstance.columns = this.selectedUdf.content.rowColumns;
    modalRef.result.then(() => { }, () => { });
  }

  public closePanel(): void {
    this.closePreviewPanel.emit(true);
  }

  public onSelectFile(eventId: string): void {
    const selectedFile = this.files.find(x => x.eventId === +eventId);
    this.columns = selectedFile.columns;
  }

  public clearParameter(): void {
    this.fgParameter.controls.udfParameterBuilder.setValue('');
    this.udfName = this.udfParams = '';
    this.UdfParameter = [];
  }

  public addParameter(param: string): void {

    const dataFrame = this.fgParameter.controls.dataFrame.value;

    if (!dataFrame) {
      this.snackbar.open('Please select value');
      return;
    }

    let udf = this.UdfParameter.find(x => x.dataFrame === dataFrame);
    this.udfName = this.fg.controls.udfsyntax.value.substring(0, this.fg.controls.udfsyntax.value.indexOf('('));

    if (udf && param === 'dataFrame') {
      this.snackbar.open(`${dataFrame} DataFrame already added`);
    } else if (param === 'dataFrame') {
      if (!udf) {
        const parameters = {
          dataFrame: `df${this.fgParameter.controls.dataFrame.value}`,
          parameter: this.fgParameter.controls.parameter.value,
          variable: this.fgParameter.controls.variable.value
        } as UdfParameter;
        udf = parameters;
        this.UdfParameter.push(parameters);
        this.udfParams += `${parameters.dataFrame},`;
      }
    } else if (param === 'parameter') {
      if (!this.fgParameter.controls.parameter.value) {
        this.snackbar.open('Please select value');
        return;
      } else {
        this.udfParams += `"${this.fgParameter.controls.parameter.value}",`;
      }
    } else if (param === 'variable') {
      this.udfParams += `"${this.fgParameter.controls.variable.value}",`;
    }

    this.fgParameter.controls.udfParameterBuilder.setValue(`${this.udfName}(${this.udfParams})`);
  }
}

export interface UdfParameter {
  dataFrame: string;
  parameter: string;
  variable: string;
}