import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { environment } from 'environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private tokenKey = 'auth_token';

  constructor(private http: HttpClient) {}

  /**
   * Faz o login do usuário e salva o token no localStorage.
   * @param credentials - Objeto contendo username e password.
   * @returns Observable com a resposta da requisição.
   */
  login(credentials: { username: string; password: string }): Observable<any> {
    return this.http.post<any>(`${environment.API}auth/login`, credentials);
  }

  saveToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  resetPassword(email: string): Observable<any> {
    return this.http.post<any>(`${environment.API}auth/forgot-password`, { email });
  }

  /**
   * Retorna o payload decodificado do usuário (claims do JWT).
   */
  getUser(): any {
    const token = this.getToken();
    if (!token) return null;

    try {
      const decodedToken = jwtDecode<any>(token);
      return decodedToken;
    } catch (e) {
      return null;
    }
  }

  /**
   * Verifica se o token é válido e não está expirado.
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const decoded: any = jwtDecode<any>(token);

      if (!decoded || !decoded.exp) {
        return false;
      }

      const currentTime = Date.now() / 1000; // em segundos
      if (decoded.exp < currentTime) {
        this.logout();
        return false;
      }

      return true;
    } catch (error) {
      this.logout();
      return false;
    }
  }

  /**
   * (Opcional) valida o token no backend, para garantir que ainda é aceito.
   */
  validateTokenWithBackend(): Observable<any> {
    return this.http.get<any>(`${environment.API}auth/validate`);
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }
}
