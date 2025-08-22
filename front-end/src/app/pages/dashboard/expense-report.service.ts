import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'environments/environment';

export interface ChartDataDTO {
  label: string;
  value: number;
}

@Injectable({
  providedIn: 'root'
})
export class ExpenseReportService {
  private readonly API = `${environment.API}api/reports/expenses`;

  constructor(private http: HttpClient) { }

  getByCategory(filters: any): Observable<ChartDataDTO[]> {
    return this.http.get<ChartDataDTO[]>(`${this.API}/by-category`, { params: this.buildParams(filters) });
  }

  getByMonth(filters: any): Observable<ChartDataDTO[]> {
    return this.http.get<ChartDataDTO[]>(`${this.API}/by-month`, { params: this.buildParams(filters) });
  }

  getBySupplier(filters: any): Observable<ChartDataDTO[]> {
    return this.http.get<ChartDataDTO[]>(`${this.API}/by-supplier`, { params: this.buildParams(filters) });
  }

  getByPaymentMethod(filters: any): Observable<ChartDataDTO[]> {
    return this.http.get<ChartDataDTO[]>(`${this.API}/by-payment-method`, { params: this.buildParams(filters) });
  }

  getByPayer(filters: any): Observable<ChartDataDTO[]> {
    return this.http.get<ChartDataDTO[]>(`${this.API}/by-payer`, { params: this.buildParams(filters) });
  }
  

   // --- KPIs ---
  getKpis(filters: any): Observable<any> {
    return this.http.get(`${this.API}/kpis`, { params: this.buildParams(filters) });
  }

  // --- Utilitário para montar parâmetros ---
  private buildParams(filters: any): HttpParams {
    let params = new HttpParams();

    if (!filters) {
      return params;
    }

    Object.keys(filters).forEach(key => {
      const value = filters[key];
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, value);
      }
    });

    return params;
  }

  
}
