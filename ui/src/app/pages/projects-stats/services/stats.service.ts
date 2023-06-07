import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StatsService {

  constructor(private http: HttpClient) { }

  getProjectStats(userId: number, projectId: number, projectType: string, params: any): Observable<any> {
    // return this.http.get(`/QuantumSparkServiceAPI/api/v1/project-stats/${userId}/${projectId}`);
    return this.http.get(`/QuantumSparkServiceAPI/api/v2/project-stats/${projectType}/${userId}/${projectId}?kpi=${params}`);
  }

  // get Kpi data
  getKpiData(userId: number, projectId: number,): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/project-stats/kpi/${userId}/${projectId}`);
  }

  // update kpi data
  public updateKpi(data: any): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/project-stats/kpi`, data);
  }
}
