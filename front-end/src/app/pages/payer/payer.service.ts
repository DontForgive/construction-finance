import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from '../login/auth.service';
import { Payer } from './Payer';
import { environment } from 'environments/environment';
import { ApiResponse } from 'app/utils/response';

@Injectable({
  providedIn: 'root'
})
export class PayerService {


  private readonly API = `${environment.API}`


  constructor(private httpClient: HttpClient, private authService: AuthService) { }


  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }


  getPayers(page: number = 0, size: number = 10, sort: string = 'id', dir: string = 'ASC', name?: string) {
    const params: any = {
      page: page,
      size: size,
      sort: sort,
      dir: dir,
      name: ''
    };

    if (name) params.name = name;

    return this.httpClient.get<ApiResponse<Payer>>(`${this.API}payer`, {
      params: params,
      headers: this.getAuthHeaders()
    });
  }

  createPayer(payer: Payer) {
    return this.httpClient.post<ApiResponse<Payer>>(
      `${this.API}payer`,
      payer,
      {
        headers: this.getAuthHeaders()
      }
    );
  }

  updatePayer(id: number, data:{name: string}) {
    return this.httpClient.put<ApiResponse<Payer>>(
      `${this.API}payer/${id}`,
      data,
      {
        headers: this.getAuthHeaders()
      }
    );
  }

  deletePayer(id: number) {
    return this.httpClient.delete(`${this.API}payer/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

}