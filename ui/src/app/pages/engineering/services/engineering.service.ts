import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { GraphParam, CreateGraphParam, PreviewParam } from 'src/app/pages/engineering/models/graph-param';

@Injectable({
  providedIn: 'root'
})
export class EngineeringService {

  constructor(private http: HttpClient) { }

  getSavedGraph(param: GraphParam): Observable<any> {
    const api = `/QuantumSparkServiceAPI/api/v1/graph/${param.projectId}/${param.userId}/${param.flowId}`;
    return this.http.get(api);
  }

  saveGraph(graph: CreateGraphParam): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/graph`, graph);
  }

  getPreviewContent(previewParam: PreviewParam) {
    const api = `/QuantumSparkServiceAPI/api/v1/preview/all/${previewParam.projectId}/${previewParam.userId}/${previewParam.engFlowId}`;
    return this.http.get(api);
  }

  createFileEvent(data: any): Observable<any> {
    return this.http.post(`/QSCommonService/api/v1/ops/file`, data);
  }

  createJoinEvent(data: any): Observable<any> {
    return this.http.post(`/QSCommonService/api/v1/ops/join`, data);
  }

  createAggregateEvent(data: any): Observable<any> {
    return this.http.post(`/QSCommonService/api/v1/ops/aggregate`, data);
  }

  deleteEvent(projectId: number, eventId: number): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/event/${projectId}/${eventId}`, {});
  }

  getEngineeringJobList(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/engFlowActive/${userId}/${projectId}`);
  }

  getEngineeringList(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/flow/${userId}/${projectId}`);
  }

  getFolders(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/getprojectfilesinfo/${projectId}/${userId}`);
  }

  saveFlow(saveParams): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/flow`, saveParams);
  }

  editFlow(editParams): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/flow/updateEngFlowName`, editParams);
  }

  deleteFlow(projectId: number, userId: number, flowId: number): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/flow/${projectId}/${userId}/${flowId}`, {});
  }

  createPgsqlFileEvent(data: any): Observable<any> {
    return this.http.post(`/QSCommonService/api/v1/ops/db`, data);
  }

  destroySession(userId: number): Observable<any> {
    return this.http.delete(`/QuantumSparkServiceAPI/api/v1/ops/${userId}`);
  }

  createUdfEvent(data: any): Observable<any> {
    return this.http.post(`/QSCommonService/api/v1/ops/udf`, data);
  }

  getUdfData(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/udf/${projectId}/${userId}`);
  }
}
