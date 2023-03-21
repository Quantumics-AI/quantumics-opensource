import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UdfRequestModel } from '../models/udfRequestModel';

@Injectable({
  providedIn: 'root'
})
export class UdfService {

  constructor(private http: HttpClient) { }

  public getUdfList(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/udf/${projectId}/${userId}`);
  }

  public save(udfRequestModel: UdfRequestModel): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/udf`, udfRequestModel);
  }

  public update(udfRequestModel: UdfRequestModel): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/udf`, udfRequestModel);
  }

  public deleteUdf(projectId: number, userId: number, udfId: number): Observable<any> {
    return this.http.delete(`/QuantumSparkServiceAPI/api/v1/udf/${projectId}/${userId}/${udfId}`);
  }
}
