import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { Upload } from '.././models/upload';
import { ProjectParam } from 'src/app/pages/projects/models/project-param';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Folder } from '../../models/folder';

@Injectable({
  providedIn: 'root'
})
export class SourceDataService {
  constructor(private http: HttpClient, private quantumfacade: Quantumfacade) { }

  getFolders(projectParam: ProjectParam): Observable<any> {
    // TO-DO
    // added work around to clean the previous state. Need to find correct way
    if (projectParam.projectId === '-1') {
      return of({ code: 200, result: [] });
    }

    const headers = new HttpHeaders().set('Content-Type', 'application/json');

    let id = projectParam.projectId;
    if (id === undefined || id === null) {
      id = projectParam.userId;
    }

    return this.http.get(`/QuantumSparkServiceAPI/api/v1/folders/${id}/${projectParam.userId}`, { headers }).pipe(
      map(response => {
        return response;
      })
    );
  }
  getFiles(projectParam: ProjectParam): Observable<any> {
    // TO-DO
    // added work around to clean the previous state. Need to find correct way
    if (projectParam.folderId === '-1') {
      return of({ code: 200, result: [] });
    }

    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http
      .get(`/QuantumSparkServiceAPI/api/v1/files/${projectParam.projectId}/${projectParam.userId}/${projectParam.folderId}`, {
        headers
      })
      .pipe(
        map(response => {
          return response;
        })
      );
  }

  // TODO - need to remove this method from here
  upload(formData: File, uploadModel: Upload) {
    const headers = new HttpHeaders();
    // headers.append('Accept', 'application/json');
    headers.append('Content-Type', 'multipart/form-data');
    const form = new FormData();
    form.append(
      'file',
      new Blob([formData], { type: 'text/csv;charset=utf-8;' }),
      formData.name
    );

    return this.http
      .post(`/QuantumSparkServiceAPI/api/v1/upload/${uploadModel.project_id}/${uploadModel.folder_id}`,
        form,
        { headers }
      )
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }

  createFolder(folder: Folder): Observable<any> {
    folder.dataPrepFreq = 'Today';
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/folders`, folder, { headers }).pipe(
      map((response: any) => {
        return response;
      })
    );
  }

  getFileContent(projectParam: any): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http
      .get(
        `/QuantumSparkServiceAPI/api/v1/files/content/${projectParam.projectId}/${projectParam.folderId}/${projectParam.fileId}`,
        { headers }
      )
      .pipe(
        map((response: any) => {
          console.log('Service-getfilecontent-response:', response);
          return response;
        })
      );
  }
}
