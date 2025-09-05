import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from '../login/auth.service';
import { ApiResponse } from 'app/utils/response';
import { User } from './user';
import { environment } from 'environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

private readonly API = `${environment.API}`
  
constructor(private httpClient: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  getUsers(
    page: number = 0,
    size: number = 10,
    sort: string = 'id',
    dir: string = 'ASC',
    username?: string, email?: string) {
    const params: any = {
      page: page,
      size: size,
      sort: sort,
      dir: dir,
      username: '',
      email: ''
    };

    if (username) params.username = username;
    if (email) params.email = email;

    return this.httpClient.get<ApiResponse<User>>(`${this.API}users`, {
      params: params,
      headers: this.getAuthHeaders()
    });

  }


}
