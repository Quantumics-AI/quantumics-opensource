import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DbConnectorService {

  constructor(private http: HttpClient) { }

  testConnection(connectionParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/test-connect`, connectionParams, { headers });
  }

  savePipeline(connectionParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/savePipeline`, connectionParams, { headers });
  }

  connect(connectionParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/getdetails/schema`, connectionParams, { headers });
  }

  tables(connectionParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/getdetails/table`, connectionParams, { headers });
  }

  tablesData(connectionParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/getdetails/metadata`, connectionParams, { headers });
  }

  preview(connectionParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/preview`, connectionParams, { headers });
  }

  // download(connectionParams): Observable<any> {
  //   const headers = new HttpHeaders().set('Content-Type', 'application/json');
  //   return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/download`, connectionParams, { headers });
  // }

  piiIdentify(connectionParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/identifyPII`, connectionParams, { headers });
  }

  getDbTypes(): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/db/types`, { headers });
  }

  getDbDetails(userId: number, dbtype: string): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/db/details/${userId}/${dbtype}`, { headers });
  }

  downloadTable(requestParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/download`, requestParams, { headers });
  }

  getConnectionBySourceType(projectId: number, sourceType: string): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.get(`/QSCommonService/api/v1/datasourcesinfo/${projectId}/${sourceType}`, { headers });
  }

  saveConnection(requestParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QSCommonService/api/v1/datasourcesinfo`, requestParams, { headers });
  }

  getPExistingipelineData(projectId: number, userId: number, pgsqlType: string): Observable<any>{
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/db/connectorList/${projectId}/${userId}/${pgsqlType}`, { headers });
  }

  getPipelineData(projectId: number, userId: number): Observable<any>{
    // const type = params.type;
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/pipeline/all/${projectId}/${userId}`, { headers });
  }

  getPipelineFolderData(projectId: number, userId: number, pipeLineId): Observable<any>{
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/pipeline/folderDetails/${projectId}/${userId}/${pipeLineId}`, { headers });
  }

  updatePipelineData(connectionParams): Observable<any>{
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/updatepipelineinfo`,connectionParams, { headers });
  }

  deletePipeline(projectId: number, userId: number, pipelineId: number): Observable<any>{
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.delete(`/QuantumSparkServiceAPI/api/v1/pipeline/delete/${projectId}/${userId}/${pipelineId}`, { headers });
  }

  getPipelineExecuteData(projectId: number, userId: number, pipelineId: number): Observable<any>{
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QSCommonService/api/v1/pipeline/executepipeline/${projectId}/${userId}/${pipelineId}`, { headers });
  }

  getPipelineFileData(projectId: number, userId: number, pipelineId: number): Observable<any>{
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/pipeline/transcationdetails/${projectId}/${userId}/${pipelineId}`, { headers });
  }

  completeDB(connectionParams): Observable<any>{
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/db/completeDBPipeline`,connectionParams, { headers });
  }

  // QSCommonService/api/v1/pipeline/executepipeline/109/96/365
}
