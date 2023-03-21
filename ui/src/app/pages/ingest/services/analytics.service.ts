import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {

  constructor(private http: HttpClient) { }

  public getAnlyticsData(projectId: number, folderId: number, fileId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/columnanalytics/${projectId}/${folderId}/${fileId}`);
  }
}
