import { HttpClient, HttpHeaders, HttpRequest, HttpEvent } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { AuthService } from '../login/auth.service';
import { Observable } from 'rxjs';
import { PhotoDTO } from './Photos';
import { ApiResponseTest } from 'app/utils/response';

@Injectable({
  providedIn: 'root'
})
export class ImagesService {

  private readonly API = `${environment.API}photos`;

  constructor(private httpClient: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  /**
   * Upload simples (sem progresso, só retorna quando concluir)
   */
  upload(file: File): Observable<ApiResponseTest<PhotoDTO>> {
    const formData = new FormData();
    formData.append('file', file);

    return this.httpClient.post<ApiResponseTest<PhotoDTO>>(`${this.API}`, formData, {
      headers: this.getAuthHeaders()
    });


  }

  uploadManyWithProgress(files: File[]): Observable<HttpEvent<ApiResponseTest<PhotoDTO[]>>> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file)); // 👈 chave "files"

    return this.httpClient.post<ApiResponseTest<PhotoDTO[]>>(
      `${this.API}`,
      formData,
      {
        headers: this.getAuthHeaders(),
        reportProgress: true,
        observe: 'events'
      }
    );
  }


  /**
   * Upload com progresso (emite eventos HttpEvent)
   */
  uploadWithProgress(file: File): Observable<HttpEvent<ApiResponseTest<PhotoDTO>>> {
    const formData = new FormData();
    formData.append('file', file);

    const req = new HttpRequest('POST', this.API, formData, {
      headers: this.getAuthHeaders(),
      reportProgress: true
    });

    return this.httpClient.request<ApiResponseTest<PhotoDTO>>(req);
  }


  listYears(): Observable<ApiResponseTest<number[]>> {
    return this.httpClient.get<ApiResponseTest<number[]>>(`${this.API}`, {
      headers: this.getAuthHeaders()
    });
  }

  listMonths(year: number): Observable<ApiResponseTest<number[]>> {
    return this.httpClient.get<ApiResponseTest<number[]>>(`${this.API}/${year}`, {
      headers: this.getAuthHeaders()
    });
  }

  listPhotos(year: number, month: number): Observable<ApiResponseTest<PhotoDTO[]>> {
    return this.httpClient.get<ApiResponseTest<PhotoDTO[]>>(`${this.API}/${year}/${month}`, {
      headers: this.getAuthHeaders()
    });
  }

  getAll(): Observable<ApiResponseTest<PhotoDTO[]>> {
    return this.httpClient.get<ApiResponseTest<PhotoDTO[]>>(`${this.API}/all`, {
      headers: this.getAuthHeaders()
    });
  }

  deletePhoto(year: number, month: number, name: string): Observable<ApiResponseTest<PhotoDTO>> {
    return this.httpClient.delete<ApiResponseTest<PhotoDTO>>(`${this.API}/${year}/${month}/${name}`, {
      headers: this.getAuthHeaders()
    });
  }
}
