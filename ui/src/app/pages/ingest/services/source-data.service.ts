import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { Upload } from '.././models/upload';
import { Quantumfacade } from 'src/app/state/quantum.facade';
import { Folder } from '../../models/folder';
import { AwsData } from '../models/awsdata';

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

  // Source connection 

  saveSourceData(data: AwsData): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/aws/save`, data, { headers }).pipe(
      map((response: any) => {
        return response;
      })
    );
  }

  public getSourceData(projectId: number, userId: number,  pageNumber: number, sizeLength: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/aws/getConnections/${projectId}/${userId}?page=${pageNumber}&size=${sizeLength}`);
  }

  updateSourceData(sourceId: number, data: any): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/aws/update/${sourceId}`, data);
  }

  deleteSourceData(projectId: number, userId: number, sourceId: number): Observable<any> {
    return this.http.delete(`/QuantumSparkServiceAPI/api/v1/aws/delete/${projectId}/${userId}/${sourceId}`)
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }

  public getAccessTypes(): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/aws/getAwsAccessTypes`);
  }

  public getBucketList(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/aws/buckets/${userId}/${projectId}`);
  }

  // http://localhost:8080/QuantumSparkServiceAPI/api/v1/aws/testConnection/1/1
  public testConnection(userId: number, projectId: number, data: any): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/aws/testConnection/${userId}/${projectId}`, data, { headers }).pipe(
      map((response: any) => {
        return response;
      })
    );
  }

  // Search functionality - connection
  public getSearchConnection(projectId: number, userId: number, connectionName: string, filter: boolean,  pageNumber: number, sizeLength: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/aws/getConnectionByName/${projectId}/${userId}/${connectionName}?filter=${filter}&page=${pageNumber}&size=${sizeLength}`);
  }

  // connnection is-exist 
  public getIsExistConnection(projectId: number, userId: number, connectionName: string,  pageNumber: number, sizeLength: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/aws/getConnectionByName/${projectId}/${userId}/${connectionName}?page=${pageNumber}&size=${sizeLength}`);
  }

  // bucket list with region
  public getBucketData(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/aws/bucketregions/${userId}/${projectId}`);
  }

  // region list
  public getRegionList(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/aws/regions/${userId}/${projectId}`);
  }
}
