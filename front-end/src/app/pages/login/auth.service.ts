import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {BehaviorSubject, catchError, Observable, throwError, timeout} from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { environment } from 'environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private tokenKey = 'auth_token';
  private tokenSubject = new BehaviorSubject<string | null>(this.readTokenFromStorage());


  constructor(private http: HttpClient) {}

  /**
   * Faz o login do usuário e salva o token no localStorage.
   * @param credentials - Objeto contendo username e password.
   * @returns Observable com a resposta da requisição.
   */
  login(credentials: { username: string; password: string }): Observable<{ token: string }> {
    return this.http
      .post<{ token: string }>(`${environment.API}auth/login`, credentials)
      .pipe(
        timeout(15000), // 15s: se o servidor não responder, estoura erro de timeout
        catchError((err) => throwError(() => this.normalizeHttpError(err)))
      );
  }

  private normalizeHttpError(err: unknown): Error {
    // Timeout do RxJS
    if (err && typeof err === 'object' && (err as any).name === 'TimeoutError') {
      return new Error('Tempo de resposta excedido. Verifique sua conexão e tente novamente.');
    }

    if (err instanceof HttpErrorResponse) {
      // status 0 normalmente = offline, DNS, CORS, servidor fora do ar
      if (err.status === 0) {
        return new Error('Não foi possível conectar ao servidor. Verifique sua internet ou tente mais tarde.');
      }

      // tenta extrair mensagem do backend
      const backendMessage =
        (typeof err.error === 'string' && err.error) ||
        (err.error?.message as string) ||
        (Array.isArray(err.error?.erros) && err.error.erros[0]);

      if (backendMessage) { return new Error(backendMessage); }

      // fallback por status
      if (err.status === 401) { return new Error('Usuário ou senha inválidos.'); }
      if (err.status === 403) { return new Error('Acesso negado.'); }
      if (err.status >= 500) { return new Error('Servidor indisponível no momento. Tente novamente mais tarde.'); }

      return new Error(`Falha na requisição (${err.status}).`);
    }

    return new Error('Erro inesperado ao tentar fazer login.');
  }

  saveToken(token: string): void {
    sessionStorage.setItem(this.tokenKey, token);
    this.tokenSubject.next(token);
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }

  resetPassword(email: string): Observable<any> {
    return this.http.post<any>(`${environment.API}auth/forgot-password`, { email });
  }

  /**
   * Retorna o payload decodificado do usuário (claims do JWT).
   */
  getUser(): any {
    const token = this.getToken();
    if (!token) { return null; }

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
    if (!token) { return false; }

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
    sessionStorage.removeItem(this.tokenKey);
    this.tokenSubject.next(null);
  }

  private readTokenFromStorage(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }
}
