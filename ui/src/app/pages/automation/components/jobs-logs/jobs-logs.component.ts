import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-jobs-logs',
  templateUrl: './jobs-logs.component.html',
  styleUrls: ['./jobs-logs.component.scss']
})
export class JobsLogsComponent implements OnInit {

  batchJobLog: any;

  ngOnInit(): void {
    const batchJobLog = localStorage.getItem('batchJobLog');
    if (batchJobLog !== 'undefined') {
      try {
        this.batchJobLog = JSON.parse(batchJobLog);
      } catch (error) {
        this.batchJobLog = [batchJobLog];
      }
    } else {
      this.batchJobLog = [];
    }
  }
}
