import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class FoldersService {

  constructor(private http: HttpClient) { }

  // getSourceTypes(): Observable<any> {
  //   return this.http.get('/QSCommonService/api/v1/datasourcetypes').pipe(
  //     map((res: any) => res.result)
  //   );
  // }

  upload(formData: File, uploadModel: any): Observable<any> {
    const form = new FormData();
    form.append('file', new Blob([formData], { type: 'text/csv;charset=utf-8;' }), formData.name);

    return this.http
      .post(`/QuantumSparkServiceAPI/api/v1/upload/${uploadModel.project_id}/${uploadModel.user_id}/${uploadModel.folder_id}`, form)
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }

  uploadIncremental(userId: number, projectId: number, folderId: number, formData: File): Observable<any> {
    const form = new FormData();
    form.append('file', new Blob([formData], { type: 'text/csv;charset=utf-8;' }), formData.name);

    return this.http
      .post(`/QuantumSparkServiceAPI/api/v1/incrementalupload/${projectId}/${userId}/${folderId}`, form)
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }

  saveApiDetails(data: any): Observable<any> {
    return this.http.post('/QuantumSparkServiceAPI/api/v1/uploadapidataasfile', data).pipe(
      map((res: any) => res.result)
    );
  }

  updateFolder(projectId: number, userId: number, folderId: number, data: any): Observable<any> {
    return this.http.put(`/QuantumSparkServiceAPI/api/v1/folders/${projectId}/${userId}/${folderId}`, data);
  }

  downloadAPIFiles(data: any): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/uploadapidataasfile`, data);
  }

  validateFileUpload(formData: File, projectId: number, userId: number, folderId: number): Observable<any> {
    const form = new FormData();
    form.append('file', new Blob([formData], { type: 'text/csv;charset=utf-8;' }), formData.name);

    return this.http.post(`/QuantumSparkServiceAPI/api/v1/validatefileupload/${projectId}/${userId}/${folderId}`, form)
      .pipe(
        map((response: any) => {
          return response;
        })
      );
  }

  uploadPiiFile(userId: number, projectId: number, folderId: number, data: any): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/upload/${projectId}/${userId}/${folderId}`, data);
  }

  uploadexternaldata(projectId: number, userId: number, data: any): Observable<any> {
    return this.http.post(`/QuantumSparkServiceAPI/api/v1/uploadexternaldata/${projectId}/${userId}`, data);
  }

  getFolders(projectId: number, userId: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/folders/${projectId}/${userId}`);
  }

  getSourceTypes(): Observable<any> {
    return this.http.get('/QSCommonService/api/v1/datasourcetypes');
  }
}
