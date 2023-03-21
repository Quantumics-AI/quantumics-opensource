import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(private http: HttpClient) { }

  public getNotification(projectId: number, userId: number, status: string | 'unread|all|count'): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/notifications/${projectId}/${userId}/${status}`);
  }

  public hideNotification(notificationId: number): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/notifications/${notificationId}`, {});
  }
}
