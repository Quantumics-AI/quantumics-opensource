import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { Upload } from '.././models/upload';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Folder } from '../../models/folder';

@Injectable({
  providedIn: 'root'
})
export class SourceDataService {
  constructor(private http: HttpClient, private quantumfacade: Quantumfacade) { }

  getFolders(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/folders/${projectId}/${userId}`).pipe(
      map((res: any) => res.result)
    );
  }

  getFiles(projectId: number, userId: number, folderId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI//api/v1/files/${projectId}/${userId}/${folderId}`).pipe(
      map((res: any) => res.result)
    );
  }

  getFileContent(projectParam: any): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http
      .get(
        `/QuantumSparkServiceAPI/api/v1/files/content/${projectParam.projectId}/${projectParam.userId}/${projectParam.folderId}/${projectParam.fileId}`,
        { headers }
      )
      .pipe(
        map((response: any) => {
          console.log('Service-getfilecontent-response:', response);
          return response;
        })
      );
  }

  deleteFolder(projectId: number, userId: number, folderId: number): Observable<any> {
    return this.http.delete(`/QuantumSparkServiceAPI/api/v1/folders/${projectId}/${userId}/${folderId}`)
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }

  deleteFile(projectId: number, userId: number, folderId: number, fileId: number): Observable<any> {
    return this.http.delete(`/QuantumSparkServiceAPI/api/v1/files/${projectId}/${userId}/${folderId}/${fileId}`)
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

  public getListData(projectId: number, userId: number, folderId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/${projectId}/${userId}/${folderId}`);
  }
}
