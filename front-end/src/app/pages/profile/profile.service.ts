import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { AuthService } from '../login/auth.service';
import { ApiResponse } from 'app/utils/response';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private readonly API = `${environment.API}`

  constructor(private httpClient: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  getProfile(){
    return this.httpClient.get(`${this.API}users/profile`, { headers: this.getAuthHeaders() });
  }

  updatePassword(password: string, newPassword: string, confirmNewPassword: string) {
      return this.httpClient.put<ApiResponse<any>>(
        `${this.API}users/update/password`,
        {
          password: password,
          newPassword: newPassword,
          confirmNewPassword: confirmNewPassword
        },
        { headers: this.getAuthHeaders() }
      );
    }

}
