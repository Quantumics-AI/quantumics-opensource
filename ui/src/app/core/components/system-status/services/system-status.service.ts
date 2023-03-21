import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SystemStatusService {

  constructor(private http: HttpClient) { }

  getSystemStatus(userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/appsysteminfo/${userId}`);
  }
}
