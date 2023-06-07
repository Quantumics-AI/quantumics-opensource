import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
// import { ProjectParam } from '../models/project-param';
// import { Project, UdfData } from '../models/project';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class WorkspaceService {

  constructor(private http: HttpClient) { }

  getProject(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/projects/${projectId}/${userId}`);
  }

  saveProject(projectId: number, projectName: string, projectDescription: string, userId: number, markAsDefault: boolean = false): Observable<any> {
    const d = {
      displayName: projectName,
      description: projectDescription,
      userId,
      markAsDefault: markAsDefault
    };
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/projects/updateproject/${projectId}`, d);
  }

  // createProject(project: Project): Observable<any> {
  //   return this.http.post(`/QuantumSparkServiceAPI/api/v1/projects`, project);
  // }

  addProjectUser(userId: number, projectId: number, email: string, name: string): Observable<any> {
    const d = {
      userEmail: email,
      userRole: 'user',
      projectId,
      userFirstName: name,
      parentUserId: userId
    };
    return this.http.post(`/QSUserService/api/v2/users/createuser`, d);
  }

  getProjectUsers(userId: number, projectId: number): Observable<any> {
    return this.http.get(`/QSUserService/api/v2/users/subusers/${userId}/${projectId}`);
  }

  deleteProjectUser(userId: number): Observable<any> {
    return this.http.post(`/QSUserService/api/v2/users/${userId}`, {});
  }

  deleteProject(projectId: number): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/projects/deactivate/${projectId}`, {}).pipe(
      map((response: any) => {
        return response;
      })
    );
  }

  createSession(userId: number, projectId: number, priceId: string): Observable<any> {
    const d = {
      priceId,
      userId,
      projectId
    };

    const headers = new HttpHeaders().set('Content-Type', 'application/json');

    return this.http.post(`/QuantumSparkServiceAPI/api/v1/stripe/creatstripecheckoutsession`, d, { headers });
  }

  getModuleDetails(): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/productfeatures`);
  }

  uploadProjectLogo(projectId: number, formData: File): Observable<any> {
    const form = new FormData();
    form.append('file', new Blob([formData]), formData.name);

    return this.http.post(`/QuantumSparkServiceAPI/api/v1/projects/uploadImage/${projectId}`, form);
  }

  restoreProject(projectId: number): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/projects/reinstate/${projectId}`, {});
  }

  getProjectAuditing(projectId: number, userId: number, startDate: string, endDate: string): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/auditevents/${projectId}/${userId}`,
      {
        'startDate': startDate ? `${startDate} 00:00:00.000` : '',
        'endDate': endDate ? `${endDate} 23:59:59.000` : ''
      });
  }

  getProjectuserinfo(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QSUserService/api/v2/projectuserinfo/${projectId}/${userId}`);
  }

  // createUdf(udf: UdfData): Observable<any> {
  //   return this.http.post(`/QuantumSparkServiceAPI/api/v1/udf`, udf);
  // }

  getUdf(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/udf/${projectId}/${userId}`);
  }

  deleteUdfData(projectId: number, userId: number, udfId: number): Observable<any> {
    return this.http.delete(`/QuantumSparkServiceAPI/api/v1/udf/${projectId}/${userId}/${udfId}`, {});
  }

  // updateUdf(udf: UdfData): Observable<any> {
  //   return this.http.put(`/QuantumSparkServiceAPI/api/v1/udf`, udf);
  // }

  getPlans(): Observable<any> {
    return this.http.get(`/QSUserService/api/v1/subscriptions`);
  }

  getCardDetails(userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/cardsinfo/${userId}`);
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


