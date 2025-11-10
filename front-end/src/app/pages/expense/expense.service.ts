import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "environments/environment";
import { AuthService } from "../login/auth.service";
import { ApiResponse, ApiResponseTest } from "app/utils/response";
import { Expense } from "./expense";

@Injectable({
  providedIn: "root",
})
export class ExpenseService {
  private readonly API = `${environment.API}`;

  constructor(
    private httpClient: HttpClient,
    private authService: AuthService
  ) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  private formatDate(date: any): string {
    if (!date) return "";
    const d = new Date(date);
    const day = String(d.getDate()).padStart(2, "0");
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const year = d.getFullYear();
    return `${year}-${month}-${day}`; // 🔹 formato ISO (yyyy-MM-dd)
  }

  getExpenses(
    page: number = 0,
    size: number = 10,
    sort: string = "id",
    dir: string = "ASC",
    filters?: {
      description?: string;
      supplierId?: number;
      payerId?: number;
      categoryId?: number;
      paymentMethod?: string;
      startDate?: string;
      endDate?: string;
    }
  ) {
    const params: any = {
      page: page,
      size: size,
      sort: sort,
      dir: dir,
      description: "",
      supplierId: "",
      payerId: "",
      categoryId: "",
      paymentMethod: "",
      startDate: "",
      endDate: "",
    };

    if (filters) {
      if (filters.description) params.description = filters.description;
      if (filters.supplierId) params.supplierId = filters.supplierId;
      if (filters.payerId) params.payerId = filters.payerId;
      if (filters.categoryId) params.categoryId = filters.categoryId;
      if (filters.paymentMethod) params.paymentMethod = filters.paymentMethod;
      if (filters.startDate)
        params.startDate = this.formatDate(filters.startDate);
      if (filters.endDate) params.endDate = this.formatDate(filters.endDate);
    }

    return this.httpClient.get<ApiResponse<Expense>>(`${this.API}expenses`, {
      params: params,
      headers: this.getAuthHeaders(),
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
    return this.httpClient.delete<void>(`${this.API}expenses/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  uploadAttachment(expenseId: number, file: File) {
    const formData = new FormData();
    formData.append("file", file); // nome TEM que ser "file"

    return this.httpClient.put<ApiResponse<Expense>>(
      `${this.API}expenses/${expenseId}/attachment`,
      formData,
      {
        headers: new HttpHeaders({
          Authorization: `Bearer ${this.authService.getToken()}`,
        }),
      }
    );
  }

  removeAttachment(expenseId: number) {
    return this.httpClient.delete<ApiResponse<void>>(
      `${this.API}expenses/${expenseId}/attachment`,
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  generateReceipt(expenseId: number) {
    return this.httpClient.get(
      `${this.API}expenses/${expenseId}/receipt`,
      {
        headers: this.getAuthHeaders(),
        responseType: 'blob' as 'json' // importante: trata retorno binário (PDF)
      }
    );
  }

}
