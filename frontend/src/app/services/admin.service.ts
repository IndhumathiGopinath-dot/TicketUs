import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Ticket } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  analytics(): Observable<any> {
    return this.http.get(`${this.apiUrl}/analytics`);
  }

  users(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`);
  }

  agents(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/agents`);
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/users/${id}`);
  }

  assign(ticketId: number, agentId: number): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.apiUrl}/tickets/${ticketId}/assign`, { agentId });
  }
}
