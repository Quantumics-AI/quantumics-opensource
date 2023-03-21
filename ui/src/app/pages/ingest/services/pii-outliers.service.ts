import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PiiOutliersService {

  constructor(private http: HttpClient) { }

  public getPIIData(prodjectId: number, folderId: number, fileId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/contentanalytics/${prodjectId}/${folderId}/${fileId}/pii`);
  }

  public getOutliersData(prodjectId: number, folderId: number, fileId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/contentanalytics/${prodjectId}/${folderId}/${fileId}/outliers`);
  }
}
