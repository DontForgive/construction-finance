import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from '../login/auth.service';
import { ApiResponse, ApiResponseTest, Page } from 'app/utils/response';
import { ServiceContractDTO } from './service-contract.dto';
import { environment } from 'environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ServiceContractService {

  private readonly API = `${environment.API}serviceContract`

  constructor(private httpClient: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  getServiceContracts(
    page: number = 0,
    size: number = 10,
    sort: string = 'id',
    dir: 'ASC' | 'DESC' = 'ASC',
    filters?: {
      name?: string;
      description?: string;
      supplierId?: number;
      categoryId?: number;
      startDate?: Date;
      endDate?: Date;
    }
  ) {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', `${sort},${dir}`);

    if (filters?.name) {
      params = params.set('name', filters.name);
    }
    if (filters?.description) {
      params = params.set('description', filters.description);
    }
    if (filters?.supplierId) {
      params = params.set('supplierId', filters.supplierId);
    }
    if (filters?.categoryId) {
      params = params.set('categoryId', filters.categoryId);
    }

    return this.httpClient.get<ApiResponse<ServiceContractDTO>>(
      `${this.API}`,
      {
        headers: this.getAuthHeaders(),
        params: params
      }
    );
  }

  createServiceContract(serviceContract: ServiceContractDTO) {
    return this.httpClient.post<ApiResponseTest<ServiceContractDTO>>(
      `${this.API}`,
      serviceContract,
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  updateServiceContract(id: number, serviceContract: ServiceContractDTO) {
    return this.httpClient.put<ApiResponse<ServiceContractDTO>>(
      `${this.API}/${id}`,
      serviceContract,
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  deleteServiceContract(id: number) {
    return this.httpClient.delete<void>(
      `${this.API}/${id}`,
      {
        headers: this.getAuthHeaders(),     
     
      }
    );
  }

}
