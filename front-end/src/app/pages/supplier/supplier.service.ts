import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { AuthService } from '../login/auth.service';
import { ApiResponse } from 'app/utils/response';
import { Supplier } from './supplier';

@Injectable({
  providedIn: 'root'
})
export class SupplierService {

  private readonly API = `${environment.API}`


  constructor(private httpClient: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }


  getSuppliers(
    page: number = 0,
    size: number = 10,
    sort: string = 'id',
    dir: string = 'ASC',
    name?: string,
    worker?:boolean) {
    const params: any = {
      page: page,
      size: size,
      sort: sort,
      dir: dir,
      name: '',
      worker: ''
    };

    if (name) params.name = name;
    if (worker) params.worker = worker;

    return this.httpClient.get<ApiResponse<Supplier>>(`${this.API}supplier`, {
      params: params,
      headers: this.getAuthHeaders()
    });

  }


  createSupplier(supplier: Supplier) {
    return this.httpClient.post<ApiResponse<Supplier>>(
      `${this.API}supplier`,
      supplier,
      {
        headers: this.getAuthHeaders()
      }
    );
  }

    updateSupplier(id: number, data: { name: string; }) {
    return this.httpClient.put(`${this.API}supplier/${id}`, data, {
      headers: this.getAuthHeaders()
    });
  }

  deleteSupplier(id: number) {
    return this.httpClient.delete(`${this.API}supplier/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

}
