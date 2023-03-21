import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GovernService {

  constructor(private http: HttpClient) { }

  testConnection(connectionParams): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/glossary`, connectionParams, { headers });
  }

  public create(userId: number, data: any): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/glossary/${userId}`, data);
  }

  public update(userId: number, data: any): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/glossary/${userId}`, data);
  }

  public delete(userId: number, id: number): Observable<any> {
    return this.http.delete(`/QuantumSparkServiceAPI/api/v1/glossary/${userId}/${id}`);
  }

  public getGlossaryList(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/glossary/${projectId}/${userId}`);
  }

  public get(projectId: number, userId: number, id: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/glossary/${projectId}/${userId}/?id=${id}`);
  }

  public getColumnInfo(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/columnsinfo/${projectId}/${userId}`);
  }

  public getSnAttributes(projectId: number, userId: number, id: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/glossary/snAttributes/${projectId}/${userId}/${id}`);
  }
}
