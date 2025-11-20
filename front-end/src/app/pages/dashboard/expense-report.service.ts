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


  private formatDate(date: any): string | null {
    if (!date) return null;
    return new Date(date).toISOString().split('T')[0]; // yyyy-MM-dd
  }


  // --- KPIs ---
  getKpis(filters: any): Observable<any> {
    return this.http.get(`${this.API}/kpis`, { params: this.buildParams(filters) });
  }

  // --- Utilitário para montar parâmetros ---
  private buildParams(filters: any): any {
    const params: any = {};

    if (filters.start) {
      params.start = this.formatDate(filters.start);
    }

    if (filters.end) {
      params.end = this.formatDate(filters.end);
    }

    if (filters.categoryId) params.categoryId = filters.categoryId;
    if (filters.supplierId) params.supplierId = filters.supplierId;
    if (filters.payerId) params.payerId = filters.payerId;

    return params;
  }



}
