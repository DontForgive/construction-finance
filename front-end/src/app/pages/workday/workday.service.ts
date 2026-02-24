import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { AuthService } from '../login/auth.service';
import { Observable } from 'rxjs';
import { WorkDay, WorkDayPaymentDTO } from './workday';
import {WorkdayBulkPaymentDTO} from './WorkdayBulkPaymentDTO';

@Injectable({
  providedIn: 'root'
})
export class WorkdayService {

  private readonly API = `${environment.API}work-days`;


  constructor(private httpClient: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  pay(dto: WorkDayPaymentDTO): Observable<any> {
    return this.httpClient.post<any>(`${this.API}/pay`, dto, {
      headers: this.getAuthHeaders()
    });
  }

  payBulk(dto: WorkdayBulkPaymentDTO, file?: File | null): Observable<any> {
    const formData = new FormData();

    formData.append('workdayIds', JSON.stringify(dto.workdayIds));
    formData.append('supplierId', String(dto.supplierId));
    formData.append('description', dto.description);
    formData.append('paymentDate', dto.paymentDate);

    formData.append('payerId', String(dto.payerId));
    formData.append('categoryId', String(dto.categoryId));
    formData.append('serviceContractId', String(dto.serviceContractId));
    formData.append('paymentMethod', dto.paymentMethod);

    formData.append('amount', String(dto.amount));

    if (file) {
      formData.append('file', file);
    }

    formData.forEach((value, key) => {
      console.log('[FormData]', key, value);
    });

    return this.httpClient.post<any>(`${this.API}/pay-bulk`, formData, {
      headers: this.getAuthHeaders()
    });
  }


  list(year: number, month: number, supplierId?: number): Observable<any> {
    let params = new HttpParams().set('year', year).set('month', month);
    if (supplierId) params = params.set('supplierId', supplierId);

    return this.httpClient.get<any>(this.API, {
      headers: this.getAuthHeaders(),
      params
    });
  }

  create(dto: WorkDay): Observable<any> {
    return this.httpClient.post<any>(this.API, dto, {
      headers: this.getAuthHeaders()
    });
  }


  update(id: number, dto: WorkDay): Observable<any> {
    return this.httpClient.put<any>(`${this.API}/${id}`, dto, {
      headers: this.getAuthHeaders()
    });
  }


  delete(id: number): Observable<any> {
    return this.httpClient.delete<any>(`${this.API}/${id}`, {
      headers: this.getAuthHeaders()
    });
  }

  payExpense(id){
    alert("PAGO!")
  }

}
