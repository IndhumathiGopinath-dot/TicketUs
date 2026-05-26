import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, User, Role } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private tokenKey = 'ticket_token';
  private userKey = 'ticket_user';

  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;

  constructor(private http: HttpClient) {
    const stored = localStorage.getItem(this.userKey);
    this.currentUserSubject = new BehaviorSubject<User | null>(stored ? JSON.parse(stored) : null);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  signup(payload: { name: string; email: string; password: string; role: Role; department?: string; }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, payload)
      .pipe(tap(resp => this.storeAuth(resp)));
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password })
      .pipe(tap(resp => this.storeAuth(resp)));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.getCurrentUser()?.role === 'ADMIN';
  }

  private storeAuth(resp: AuthResponse): void {
    localStorage.setItem(this.tokenKey, resp.token);
    const user: User = {
      id: resp.userId,
      name: resp.name,
      email: resp.email,
      role: resp.role,
      department: resp.department
    };
    localStorage.setItem(this.userKey, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }
}
