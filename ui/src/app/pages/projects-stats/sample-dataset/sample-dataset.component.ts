import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SnackbarService } from 'src/app/core/services/snackbar.service';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, ActivatedRoute } from '@angular/router';
// import { window } from 'd3-selection';

@Component({
  selector: 'app-sample-dataset',
  templateUrl: './sample-dataset.component.html',
  styleUrls: ['./sample-dataset.component.scss']
})
export class SampleDatasetComponent implements OnInit {
  @Input() userId: number;
  @Input() projectId: number;

  public fg: FormGroup;
  public selectDataset: boolean = false;
  public datasets: string;

  constructor(
    public modal: NgbActiveModal,
    private snakbar: SnackbarService,
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.fg = this.fb.group({

    })
  }

  public onItemChange(value: string): void {
    this.selectDataset = true;
    console.log(value);
    if (value == 'auto') {
      this.datasets = 'https://qsai-sample-dataset.s3.amazonaws.com/Automobile/Automobile.zip';
    }

    if (value == 'bio') {
      this.datasets = 'https://qsai-sample-dataset.s3.amazonaws.com/BioTech/BioTechnology.zip';
    }
    
    if (value == 'ins') {
      this.datasets = 'https://qsai-sample-dataset.s3.amazonaws.com/Insurance/Insurance.zip';
    }

    if (value == 'tel') {
      this.datasets = 'https://qsai-sample-dataset.s3.amazonaws.com/Telecom/Telecommunication.zip';
    }

    if (value == 'retail') {
      this.datasets = 'https://qsai-sample-dataset.s3.amazonaws.com/Retail/Ecommerce.zip';
    }

    if (value == 'bnk') {
      this.datasets = 'https://qsai-sample-dataset.s3.amazonaws.com/Banking/Banking.zip';
    }

    if (value == 'hc') {
      this.datasets = 'https://qsai-sample-dataset.s3.amazonaws.com/Healthcare/HealthCare.zip';
    }

    if (value == 'it') {
      this.datasets = 'https://qsai-sample-dataset.s3.amazonaws.com/ITServices/IT+Services.zip';
    }

    if (value == 'tp') {
      this.datasets = 'https://qsai-sample-dataset.s3.amazonaws.com/Transportation/Transportation.zip';
    }
  }
  
  public downloadDataset(): void {
    const fileName = this.datasets.substring(this.datasets.lastIndexOf('/') + 1);
    const link = document.createElement('a');
    link.setAttribute('href', this.datasets);
    link.setAttribute('download', fileName);
    link.click();
    this.modal.close();
    this.router.navigate([`projects/${this.projectId}/ingest/select-source-type`]);
  }
}
