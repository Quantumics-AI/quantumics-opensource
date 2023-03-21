import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {

  constructor(private http: HttpClient) { }

  public getSubscriptionDetails(userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/projects/subscription/${userId}`);
  }
}
