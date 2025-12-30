import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "environments/environment";
import { AuthService } from "../login/auth.service";
import { ApiResponse, ApiResponseTest } from "app/utils/response";
import { Expense } from "./expense";
import Swal from "sweetalert2";

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
      serviceContractId?: number;
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
      serviceContractId: ""
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
      if (filters.serviceContractId) params.serviceContractId = filters.serviceContractId;
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


  generateAndOpenReceipt(expenseId: number): void {
    Swal.fire({
      title: 'Gerando recibo...',
      text: 'Aguarde alguns segundos',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading()
    });

    this.generateReceipt(expenseId).subscribe({
      next: (blob: Blob) => {
        Swal.close();

        const fileURL = URL.createObjectURL(blob);
        window.open(fileURL, '_blank');

        // opcional (boa prática)
        setTimeout(() => URL.revokeObjectURL(fileURL), 10000);
      },
      error: (err) => {
        Swal.fire({
          icon: 'error',
          title: 'Erro',
          text: 'Falha ao gerar o recibo.'
        });
        console.error('Erro ao gerar recibo:', err);
      }
    });
  }

  exportExpensesToExcel({
    supplierId,
    payerId,
    categoryId,
    paymentMethod,
    startDate,
    endDate,
    serviceContractId,
  }: {
    supplierId?: number;
    payerId?: number;
    categoryId?: number;
    paymentMethod?: string;
    startDate?: string;
    endDate?: string;
    serviceContractId?: number;
  }) {

    const params: any = {
      supplierId: supplierId ?? '',
      payerId: payerId ?? '',
      categoryId: categoryId ?? '',
      paymentMethod: paymentMethod ?? '',
      startDate: startDate ? this.formatDate(startDate) : '',
      endDate: endDate ? this.formatDate(endDate) : '',
      serviceContractId: serviceContractId ?? '',
    };

    Swal.fire({
      title: 'Gerando Excel...',
      text: 'Aguarde enquanto preparamos seu relatório 📊',
      allowOutsideClick: false,
      allowEscapeKey: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    this.httpClient.post(
      `${this.API}api/reports/expenses/xlsx`,
      null,
      {
        headers: this.getAuthHeaders(),
        params,
        responseType: 'blob',
        observe: 'response' // 🔹 IMPORTANTE: Observa a resposta completa para ler os headers
      }
    ).subscribe({
      next: (response) => {
        Swal.close();

        // 🔹 Tenta extrair o nome do arquivo do header Content-Disposition
        const contentDisposition = response.headers.get('content-disposition');
        let fileName = 'despesas.xlsx'; // fallback

        if (contentDisposition) {
          const fileNameMatch = contentDisposition.match(/filename="?(.+)"?/);
          if (fileNameMatch && fileNameMatch.length > 1) {
            fileName = fileNameMatch[1];
          }
        }

        const blob = response.body as Blob;
        const fileURL = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = fileURL;
        a.download = fileName; // 🔹 Usa o nome vindo do back-end
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);

        setTimeout(() => URL.revokeObjectURL(fileURL), 10000);

        Swal.fire({
          icon: 'success',
          title: 'Excel gerado!',
          text: 'Download iniciado com sucesso 🚀',
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err) => {
        Swal.close();
        Swal.fire({
          icon: 'error',
          title: 'Erro ao gerar Excel',
          text: 'Não foi possível gerar o arquivo 😕'
        });
        console.error(err);
      }
    });
  }
}
