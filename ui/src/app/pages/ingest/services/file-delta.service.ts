import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FileDeltaService {

  constructor(private http: HttpClient) { }

  public getFileDelta(prodjectId: number, folderId: number, file1Id: number, file2Id: number): Observable<any> {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/filesdelta/${prodjectId}/${folderId}/${file1Id}/${file2Id}`);
  }

  public getFiles(projectId: number, folderId: number) {
    return this.http.get(`/QuantumSparkServiceAPI/api/v1/files/${projectId}/${folderId}`);
  }
}
