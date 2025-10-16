import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { AuthService } from '../login/auth.service';
import { Observable } from 'rxjs';
import { WorkDay } from './workday';

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

}
