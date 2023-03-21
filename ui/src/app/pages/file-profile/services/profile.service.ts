import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  constructor(private http: HttpClient) { }

  public getFileStats(projectId: number, userId: number, folderId: number, fileId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/dataprofiling/${projectId}/${userId}/${folderId}/${fileId}`);
  }

  public getDataQuality(projectId: number, userId: number, folderId: number, fileId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/dataquality/analyze/${projectId}/${userId}/${folderId}/${fileId}`);
  }

  public getFrequencyAnalysis(projectId: number, userId: number, folderId: number, fileId: number, columnName: string): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/dataprofiling/${projectId}/${userId}/${folderId}/${fileId}/${columnName}`);
  }

  public getInvalidData(projectId: number, userId: number, folderId: number, fileId: number, columnName: string): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/dataquality/${projectId}/${userId}/${folderId}/${fileId}/${columnName}`);
  }
}
