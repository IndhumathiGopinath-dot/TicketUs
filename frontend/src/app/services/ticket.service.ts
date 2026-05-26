import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Ticket, TimelineEntry, CreateTicketRequest, Category, Status } from '../models/models';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private apiUrl = `${environment.apiUrl}/tickets`;

  constructor(private http: HttpClient) {}

  create(req: CreateTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(this.apiUrl, req);
  }

  list(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(this.apiUrl);
  }

  assigned(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/assigned`);
  }

  get(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.apiUrl}/${id}`);
  }

  updateStatus(id: number, status: Status, notes?: string): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.apiUrl}/${id}/status`, { status, notes });
  }

  rate(id: number, rating: number): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.apiUrl}/${id}/rate`, { rating });
  }

  timeline(id: number): Observable<TimelineEntry[]> {
    return this.http.get<TimelineEntry[]>(`${this.apiUrl}/${id}/timeline`);
  }

  similar(title: string, category: Category): Observable<Ticket[]> {
    const params = new HttpParams().set('title', title).set('category', category);
    return this.http.get<Ticket[]>(`${this.apiUrl}/similar`, { params });
  }

  uploadAttachment(id: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/${id}/attachments`, formData);
  }

  listAttachments(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/attachments`);
  }

  downloadUrl(storedName: string): string {
    return `${this.apiUrl}/attachments/download/${storedName}`;
  }
}
