import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AutomationService {

  constructor(private http: HttpClient) { }

  public getEngineeringJobs(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/flow/${userId}/${projectId}`);
  }

  public getCleansingJobs(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/folders/${projectId}/${userId}`);
  }

  public getCleansingHistory(params: any): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/history/${params.projectId}/${params.userId}/${params.folderId}`);
  }

  public getEngineeringHistory(params: any): Observable<any> {
    return this.http.post(`/QSCommonService/api/v1/ops/engflowjobhistory`, params);
  }

  public getFilesToCleanse(params: any): Observable<any> {
    return this.http.post(`/QSCommonService/api/v1/ops/getengflowfiles`, params);
  }

  public getFiles(projectId: number, userId: number, folderId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/${projectId}/${userId}/${folderId}`);
  }

  public runAutomation(params: any): Observable<any> {
    return this.http.post(`/QSCommonService/api/v1/ops/runconfigauto`, params);
  }

  public getEngDownloadFile(userId: number, params: any): Observable<any> {
    const projectId = params.projectId;
    const engFlowId = params.engFlowId;
    const type = params.type;
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/downloadengfile/${projectId}/${userId}/${engFlowId}/${type}`,
      { responseType: 'text' });
  }

  public getCleanseDownloadFile(userId: number, params: any): Observable<any> {
    const projectId = params.projectId;
    const folderId = params.folderId;
    const fileId = params.fileId;
    const jobId = params.jobId;
    const type = params.type;

    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/download/${projectId}/${userId}/${folderId}/${fileId}/${jobId}/${type}`,
      { responseType: 'text' });
  }

  public runJob(params: any): Observable<any> {
    return this.http.post(`/QSCommonService/api/v1/runcleansejob`, params);
  }

  public deleteJobFile(projectId: number, userId: number, runJobId: number): Observable<any> {
    return this.http.delete(`/QuantumSparkServiceAPI/api/v1/runjob/job/${projectId}/${userId}/${runJobId}`);
  }

  public getProjectFilesInfo(projectId: number, engFlowId: number): Observable<any> {
    return this.http.get(`/QSCommonService/api/v1/event/${projectId}/${engFlowId}`);
  }

  public getFilesToRun(projectId: number, userId: number, engFlowId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/ops/selectfilestorun/${projectId}/${userId}/${engFlowId}`);
  }

  public getDataLineageInfo(projectId: number, userId: number, autoRunId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/ops/fetchdatalineageinfo/${projectId}/${userId}/${autoRunId}`);
  }
}
