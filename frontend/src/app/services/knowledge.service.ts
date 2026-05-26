import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { KnowledgeArticle, Category } from '../models/models';

@Injectable({ providedIn: 'root' })
export class KnowledgeService {
  private apiUrl = `${environment.apiUrl}/knowledge`;

  constructor(private http: HttpClient) {}

  all(): Observable<KnowledgeArticle[]> {
    return this.http.get<KnowledgeArticle[]>(this.apiUrl);
  }

  suggest(query?: string, category?: Category): Observable<KnowledgeArticle[]> {
    let params = new HttpParams();
    if (query) params = params.set('query', query);
    if (category) params = params.set('category', category);
    return this.http.get<KnowledgeArticle[]>(`${this.apiUrl}/suggest`, { params });
  }

  create(article: Partial<KnowledgeArticle>): Observable<KnowledgeArticle> {
    return this.http.post<KnowledgeArticle>(this.apiUrl, article);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
