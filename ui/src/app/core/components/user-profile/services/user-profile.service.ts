import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserProfile } from '../models/user-profile';

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {

  constructor(private http: HttpClient) { }

  public getUserProfile(userId: number): Observable<UserProfile> {
    return this.http.get(`/QSUserService/api/v2/users/${userId}`).pipe(
      map((res: any) => res.result)
    );
  }

  public updateUserProfile(userId: number, profile: any): Observable<any> {
    const d = {
      userFirstName: profile.firstName,
      userLastName: profile.lastName,
      userPhone: profile.phone,
      userCompany: profile.company,
      userCountry: profile.country,
      companyRole: profile.role
    };
    return this.http.put(`/QSUserService/api/v2/users/${userId}`, d);
  }

  public updateUserProfileLogo(userId: number, formData: File): Observable<any> {
    const form = new FormData();
    form.append('file', new Blob([formData]), formData.name);

    return this.http.post(`/QSUserService/api/v1/uploadImage/${userId}`, form);
  }

  public getUserProfileInfo(userId: number): Observable<any> {
    return this.http.get(`/QSUserService/api/v2/userinfo/${userId}`);
  }
}
