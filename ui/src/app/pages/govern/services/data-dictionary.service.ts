import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Dictionary } from '../models/dictionary';

@Injectable({
  providedIn: 'root'
})
export class DataDictionaryService {

  constructor(private http: HttpClient) { }

  public getData(projectId: number, folderId: number, fileId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/dictionary/${projectId}/${folderId}/${fileId}`);
  }

  public getFolders(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/folders/${projectId}/${userId}`);
  }

  public getFiles(projectId: number, userId: number, folderId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/${projectId}/${userId}/${folderId}`);
  }

  public getFileData(userId: number, projectId: number, folderId: number, fileId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/dictionary/${projectId}/${userId}/${folderId}/${fileId}`);
  }

  public save(projectId: number, userId: number, data: Dictionary): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/dictionary/${projectId}/${userId}`, data);
  }

  public update(projectId: number, userId: number, dictionaryId: number, payLoad: Dictionary): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/dictionary/${projectId}/${userId}/${dictionaryId}`, payLoad);
  }

  public deleteDictionary(projectId: number, userId: number, dictionaryId: number): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/dictionary/delete/${projectId}/${userId}/${dictionaryId}`, {});
  }

  public getProjectuserinfo(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QSUserService/api/v2/projectuserinfo/${projectId}/${userId}`);
  }
}
