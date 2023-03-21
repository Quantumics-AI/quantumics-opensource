import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Login } from '../models/login';
import { Token } from '../models/token';

@Injectable({
  providedIn: 'root'
})
export class LoginService {


  constructor(private http: HttpClient) {
  }

  login(login: Login) {
    return this.http.post(`/QSUserService/api/v2/authorization`, login).pipe(
      map((user: any) => {
        localStorage.setItem('certificate', JSON.stringify(user));
        return user;
      })
    );
  }

  getRefreshToken(refreshToken: string) {
    const payload = {
      refreshToken: refreshToken
    } as Token;

    return this.http.post(`/QSUserService/api/v2/authorization/refreshToken`, payload).pipe(
      map((token: any) => {
        return token;
      })
    );
  }

  resetPassword(email: string, password: string): Observable<any> {
    const d = {
      emailId: email,
      newPassword: password
    };

    return this.http.post(`/QSUserService/api/v2/users/resetpassword`, d);
  }

  validateEmail(guid: string): Observable<any> {
    return this.http.get(`/QSUserService/api/v2/authorization/verifyEmail/${guid}`);
  }
}
