import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FolderService {

  constructor(private http: HttpClient) { }

  public getFoldersPipelines(projectId: number, userId: number, query: boolean): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/folders/${projectId}/${userId}/?pipeline=${query}`);
  }

  public getFolders(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/folders/${projectId}/${userId}`);
  }

  public getFiles(projectId: number, userId: number, folderId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/${projectId}/${userId}/${folderId}`);
  }
}
