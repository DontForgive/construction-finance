import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { AuthService } from '../login/auth.service';
import { ApiResponse } from 'app/utils/response';
import { User } from '../user/user';
import {BehaviorSubject, Observable, tap} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private readonly API = `${environment.API}`
  private readonly profileSubject = new BehaviorSubject<User | null>(null);
  readonly profile$ = this.profileSubject.asObservable();


  constructor(private httpClient: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(isFormData: boolean = false): HttpHeaders {
    const token = this.authService.getToken();

    if (isFormData) {
      return new HttpHeaders({
        Authorization: `Bearer ${token}`
      });
    }

    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }


  getProfile(): Observable<any> {
    return this.httpClient
      .get(`${this.API}users/profile`, { headers: this.getAuthHeaders() })
      .pipe(
        tap((res: any) => {
          const data = res?.data as User | undefined;
          if (data) {
            this.profileSubject.next(data);
          }
        })
      );
  }

  refreshProfile(): void {
    this.getProfile().subscribe({
      next: () => {},
      error: () => {}
    });
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

  updateProfile(data: User | FormData) {
    const isFormData = data instanceof FormData;

    return this.httpClient.put<ApiResponse<any>>(
      `${this.API}users/profile`,
      data,
      { headers: this.getAuthHeaders(isFormData) }
    );
  }

  updateProfilePicture(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    return this.httpClient.put<ApiResponse<User>>(
      `${this.API}users/profile/picture`,
      formData,
      { headers: this.getAuthHeaders(true) }
    );
  }

  updateBanner(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    return this.httpClient.put<ApiResponse<User>>(
      `${this.API}users/profile/banner`,
      formData,
      { headers: this.getAuthHeaders(true) }
    );
  }

}

