import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { AuthService } from '../login/auth.service';
import { ApiResponse, ApiResponseTest } from 'app/utils/response';
import { Expense } from './expense';

@Injectable({
  providedIn: 'root'
})
export class ExpenseService {

  private readonly API = `${environment.API}`;

  constructor(private httpClient: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  getExpenses(
    page: number = 0,
    size: number = 10,
    sort: string = 'id',
    dir: string = 'ASC',
    filters?: {
      description?: string;
      supplierId?: number;
      payerId?: number;
      categoryId?: number;
      paymentMethod?: string;
      date?: string;
    }
  ) {
    const params: any = {
      page: page,
      size: size,
      sort: sort,
      dir: dir,
      description: '',
      supplierId: '',
      payerId: '',
      categoryId: '',
      paymentMethod: '',
      date: '',

    };

    if (filters) {
      if (filters.description) params.description = filters.description;
      if (filters.supplierId) params.supplierId = filters.supplierId;
      if (filters.payerId) params.payerId = filters.payerId;
      if (filters.categoryId) params.categoryId = filters.categoryId;
      if (filters.paymentMethod) params.paymentMethod = filters.paymentMethod;
      if (filters.date) params.date = filters.date;
    }

    return this.httpClient.get<ApiResponse<Expense>>(`${this.API}expenses`, {
      params: params,
      headers: this.getAuthHeaders()
    });
  }

  createExpense(expense: Expense) {
  return this.httpClient.post<ApiResponseTest<Expense>>(
    `${this.API}expenses`,
    expense,
    { headers: this.getAuthHeaders() }
  );
}



  updateExpense(id: number, expense: Expense) {
    return this.httpClient.put<ApiResponse<Expense>>(
      `${this.API}expenses/${id}`,
      expense,
      { headers: this.getAuthHeaders() }
    );
  }


  deleteExpense(id: number) {
    return this.httpClient.delete<void>(
      `${this.API}expenses/${id}`,
      { headers: this.getAuthHeaders() }
    );
  }

  uploadAttachment(expenseId: number, file: File) {
    const formData = new FormData();
    formData.append('file', file);

    const token = this.authService.getToken(); // pega só o token
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.httpClient.put<ApiResponse<Expense>>(
      `${this.API}expenses/${expenseId}/attachment`,
      formData,
      { headers }
    );
  }



  removeAttachment(expenseId: number) {
    return this.httpClient.delete<ApiResponse<void>>(
      `${this.API}expenses/${expenseId}/attachment`,
      {
        headers: this.getAuthHeaders()
      }
    );
  }

}
