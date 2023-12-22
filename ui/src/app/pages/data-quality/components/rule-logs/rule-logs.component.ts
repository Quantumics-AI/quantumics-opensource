import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-rule-logs',
  templateUrl: './rule-logs.component.html',
  styleUrls: ['./rule-logs.component.scss']
})
export class RuleLogsComponent implements OnInit {
  batchJobLog: any;
  projectId: number;

  ngOnInit(): void {
    this.projectId = +localStorage.getItem('project_id');
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
