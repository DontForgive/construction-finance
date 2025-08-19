import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from 'environments/environment';
import { Category } from './category';
import { ApiResponse } from 'app/utils/response';
import { Observable } from 'rxjs/internal/Observable';
import { AuthService } from '../login/auth.service';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private readonly API = `${environment.API}`

constructor(private httpClient:HttpClient,  private authService: AuthService) { }

 private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

getCategories(page: number = 0, size: number = 10, sort: string = 'id', dir: string = 'ASC') {
  const params = {
    page: page,
    size: size,
    sort: sort,
    dir: dir
  };
  return this.httpClient.get<ApiResponse<Category>>(`${this.API}categories`, {
    params: params,
     headers: this.getAuthHeaders()
  });
}

createCategory(category: Category): Observable<ApiResponse<Category>> {
  return this.httpClient.post<ApiResponse<Category>>(
    `${this.API}categories`,
    category,
    {
       headers: this.getAuthHeaders()
    }
  );
}

updateCategory(id: number, data: { name: string; description: string }) {
  return this.httpClient.put(`${this.API}categories/${id}`, data, {
     headers: this.getAuthHeaders()
  });
}

deleteCategory(id: number) {
  return this.httpClient.delete(`${this.API}categories/${id}`, {
     headers: this.getAuthHeaders()
  });
}



}
