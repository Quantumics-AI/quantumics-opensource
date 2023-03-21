import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PlansService {

  constructor(private http: HttpClient) { }

  getPlans(): Observable<any> {
    return this.http.get(`/QSUserService/api/v1/subscriptions`);
  }

  getCurrentPlanDetails(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/plandetails/${userId}/${projectId}`);
  }

  getInvoiceHistory(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/invoicedetails/${projectId}/${userId}`);
  }

  getSuccessDetails(userId: number, stripeSessionId: string): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/stripe/stripesessiondetails/${userId}/${stripeSessionId}`);
  }
}
