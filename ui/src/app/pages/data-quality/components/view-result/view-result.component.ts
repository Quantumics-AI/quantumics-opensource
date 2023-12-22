import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import {  ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Certificate } from 'src/app/models/certificate';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-view-result',
  templateUrl: './view-result.component.html',
  styleUrls: ['./view-result.component.scss']
})
export class ViewResultComponent implements OnInit {
  @Input() projectId: number;
  @Input() userId: number;
  @Input() output: any;

  public nullRule: string = "Null Value";
  public duplicateRule: string = "Duplicate Value";
  public dataComplete: string = 'Data Completeness';
  public dataProfiler: string = 'Data Profiler';
  public zeroRowCheck: string = 'Zero Row Check';
  public sourceFileName: string;
  public targetFileName: string;
  public columnName: string;
  constructor(
    public modal: NgbActiveModal,
  ){

  }

  ngOnInit(): void {
      console.log(this.output);
      if(this.output?.sourceFile){
        const sourceFileParts = this.output.sourceFile.split('/'); // Split the string by '/'
        const fileNameWithQuotes = sourceFileParts[sourceFileParts.length - 1]; // Get the last part
  
        // Remove the surrounding double quotes
        this.sourceFileName = fileNameWithQuotes.replace(/"/g, '');
      }

      if(this.output?.targetFile){
        const targetFileParts = this.output.targetFile.split('/'); // Split the string by '/'
        const fileNameWithQuotes = targetFileParts[targetFileParts.length - 1]; // Get the last part
  
        // Remove the surrounding double quotes
        this.targetFileName = fileNameWithQuotes.replace(/"/g, '');
      }

      if(this.output?.SourceFile){
        const sourceFileParts = this.output.SourceFile.split('/'); // Split the string by '/'
        const fileNameWithQuotes = sourceFileParts[sourceFileParts.length - 1]; // Get the last part
  
        // Remove the surrounding double quotes
        this.sourceFileName = fileNameWithQuotes.replace(/"/g, '');
      }

      if(this.output?.TargetFile){
        const targetFileParts = this.output.TargetFile.split('/'); // Split the string by '/'
        const fileNameWithQuotes = targetFileParts[targetFileParts.length - 1]; // Get the last part
  
        // Remove the surrounding double quotes
        this.targetFileName = fileNameWithQuotes.replace(/"/g, '');
      }

      if (this.output?.header) {
        this.columnName = this.output.header;
      }
      
  }

  toggleColumns(index: number) {
    this.output.data[index].showColumns = !this.output.data[index].showColumns;
  }
}
