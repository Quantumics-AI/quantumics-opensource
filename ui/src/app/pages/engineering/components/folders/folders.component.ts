import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CleansingDataService } from 'src/app/pages/cleansing/services/cleansing-data.service';
import { FolderService } from 'src/app/pages/file-profile/services/folder.service';
import { DbConnectorService } from 'src/app/pages/ingest/services/db-connector.service';
import { EngineeringService } from '../../services/engineering.service';

@Component({
  selector: 'app-folders',
  templateUrl: './folders.component.html',
  styleUrls: ['./folders.component.scss']
})
export class FoldersComponent implements OnInit {
  @Input() projectId: number;
  @Input() userId: number;
  @Output() folderChange = new EventEmitter<string>();
  @Output() dropFile = new EventEmitter<string>();

  public colors = ['#c1e4ff', '#87cbff', '#0C69AE', '#c1e4fD'];
  public data: Array<any> = [];

  constructor(
    private folderService: FolderService,
    private cleansingDataService: CleansingDataService,
    private engineeringService: EngineeringService,
    private dbConnectorService: DbConnectorService) { }

  ngOnInit(): void {

    this.data.push(
      {
        name: 'Raw',
        folders: []
      },
      {
        name: 'Cleansed',
        folders: []
      },
      {
        name: 'Engineered',
        folders: [],
        files: []
      },
      {
        name: 'PipeLines',
        pipelines: [],
        folders: [],
        files: []
      }
    );
  }

  drag(ev, file) {
    const fl = JSON.stringify(file);
    ev.dataTransfer.setData('text', fl);
    ev.dataTransfer.effectAllowed = 'copy';
  }

  dragClick(file: any): void {
    const fl = JSON.stringify(file);
    this.dropFile.emit(fl);
  }

  onClickPanel(type: string, data: any): void {

    if (data.isExpanded) {
      data.isExpanded = !data.isExpanded;
      return;
    }

    this.data.map(t => {
      t.isExpanded = t.name === data.name ? !t.isExpanded : false;
    });

    this.getFolder(type);
  }

  onFolderClick(type: string, data: any): void {

    if (data.isExpanded) {
      data.isExpanded = !data.isExpanded;
      return;
    }

    if (type !== 'PipeLines') {
      this.data.find(x => x.name === type).folders.map(t => {
        if (t.folderId === data.folderId) {
          data.isExpanded = !data.isExpanded;
        } else {
          t.isExpanded = false;
        }
      });
    } else {
      this.data.find(x => x.name === type).pipelines.map(t => {
        if (t.folders) {
          t.folders.map(t => {
            t.isExpanded = t.folderId === data.folderId ? !t.isExpanded : false;
          });
        }
      });
    }

    if (type === 'Raw') {
      this.folderService.getFiles(this.projectId, this.userId, data.folderId).subscribe((response: any) => {
        data.files = response.result;
        if (data.files) {
          data.files.map(t => t.category = 'raw');
        }
      });
    } else if (type === 'Cleansed') {
      // this.cleansingDataService.getAddedCleansingFiles(this.projectId, this.userId, data.folderId).subscribe((response: any) => {
      //   let files = [];

      //   response.result.map(t => {
      //     const fileId = files.findIndex(x => x.fileId === t.fileId);

      //     if (fileId === -1) {
      //       files.push(t);
      //     }
      //   });

      //   data.files = files;
      //   if (data.files) {
      //     data.files.map(t => t.category = 'processed');
      //   }

      // });
    } else if (type === 'PipeLines') {
      this.folderService.getFiles(this.projectId, this.userId, data.folderId).subscribe((response: any) => {
        data.files = response.result;
        if (data.files) {
          data.files.map(t => t.category = 'raw');
        }
      });
    }
  }

  public onPipeLineClick(type: string, pipelines: any): void {

    if (pipelines.isExpanded) {
      pipelines.isExpanded = !pipelines.isExpanded;
      return;
    }

    this.data.find(x => x.name === type).pipelines.map(t => {
      t.isExpanded = t.pipelineId === pipelines.pipelineId ? !t.isExpanded : false;
    });

    this.dbConnectorService.getPipelineFolderData(this.projectId, this.userId, pipelines.pipelineId).subscribe((response: any) => {
      pipelines.folders = response.result;
    });
  }

  private getFolder(type: string): void {

    const data = this.data.find(x => x.name === type);

    if (type === 'Raw') {
      this.folderService.getFolders(this.projectId, this.userId).subscribe((response: any) => {
        if(response.result) {
          data.folders = response.result.filter(t => !t.external);
        }
      });
    } else if (type === 'Cleansed') {
      // this.cleansingDataService.getCleansingJobs(this.projectId, this.userId).subscribe((response: any) => {
      //   data.folders = response.result;
        
      // });
      this.cleansingDataService.getCleansedJobs(this.projectId, this.userId).subscribe((response: any) => {
        data.folders = response.result;
        data.folders.map(t =>{
          t.folderName = t.name
      });
        
      });
    } else if (type === 'Engineered') {
      this.engineeringService.getEngineeringJobList(this.userId, this.projectId).subscribe((response: any) => {
        data.files = response.result[0].files;
        if (data.files) {
          data.files.map(t => {
            t.fileId = t.fileId;
            t.fileName = t.fileName,
            t.category = 'eng'
          });
        }
      });
    } else if (type === 'PipeLines') {
      this.dbConnectorService.getPipelineData(this.projectId, this.userId).subscribe((response: any) => {
        data.pipelines = response.result;
      });
    }
  }
}
