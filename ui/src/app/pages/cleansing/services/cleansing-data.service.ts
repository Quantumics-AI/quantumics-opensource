import {
  HttpClient,
  HttpHeaders
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { UpdateRule } from '../models/update-rule';

@Injectable({
  providedIn: 'root'
})
export class CleansingDataService {

  constructor(
    private http: HttpClient) {
  }

  getAllJobStatus(projectParam: any): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/runjob/all/${projectParam.userId}/${projectParam.projectId}`,
      { headers }
    )
      .pipe(
        map(response => {
          return response;
        })
      );
  }

  getFileContent(projectParam: any): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http
      .get(
        `/QuantumSparkServiceAPI/api/v1/files/content/${projectParam.projectId}/${projectParam.userId}/${projectParam.folderId}/${projectParam.fileId}`,
        { headers }
      )
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }

  addRules(addRules: any): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/rules/param/${addRules.projectId}/${addRules.userId}`, addRules, { headers })
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }

  updateRules(addRules: any): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/rules/param/${addRules.projectId}/${addRules.userId}`, addRules, { headers })
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }

  updateRuleSequence(updateRules: UpdateRule[], projectId: number): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/rules/${projectId}`, updateRules, { headers })
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }
  deleteRules(deleteRule: any): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.put(
      `/QuantumSparkServiceAPI/api/v1/rules/param/${deleteRule.projectId}/${deleteRule.userId}/${deleteRule.ruleId}`, {}, { headers })
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }
  getRules(projectParam: any): Observable<any> {
    // const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.get(
      `/QuantumSparkServiceAPI/api/v1/rules/catalogue/${projectParam.projectId}/${projectParam.userId}/${projectParam.folderId}/${projectParam.fileId}`)
      .pipe(
        map(response => {
          return response;
        })
      );
  }

  public getCleansingJobs(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/cleanse/${projectId}/${userId}`);
  }

  public getCleansedJobs(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/cleanseJobsActive/${userId}/${projectId}`);
  }

  public getCountsForStandardizeRule(projectId: number, folderId: number, fileId: number, columnName: string): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/${projectId}/${folderId}/${fileId}/${columnName}`);
  }

  public getAddedCleansingFiles(projectId: number, userId: number, folderId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/history/${projectId}/${userId}/${folderId}`).pipe(
      map(response => {
        return response;
      })
    );
  }

  public validateDataTypeCastRule(ruleData: any): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/validatedatatype`, ruleData);
  }
}
