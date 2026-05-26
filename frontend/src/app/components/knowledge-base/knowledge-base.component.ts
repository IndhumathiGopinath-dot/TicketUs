import { Component, OnInit } from '@angular/core';
import { KnowledgeService } from '../../services/knowledge.service';
import { AuthService } from '../../services/auth.service';
import { KnowledgeArticle, Category } from '../../models/models';

@Component({
  selector: 'app-knowledge-base',
  templateUrl: './knowledge-base.component.html',
  styleUrls: ['./knowledge-base.component.css']
})
export class KnowledgeBaseComponent implements OnInit {
  articles: KnowledgeArticle[] = [];
  filtered: KnowledgeArticle[] = [];
  search = '';
  filterCategory: Category | 'ALL' = 'ALL';

  showCreate = false;
  newTitle = '';
  newContent = '';
  newCategory: Category = 'IT';
  newKeywords = '';
  error = '';

  constructor(public auth: AuthService, private knowledgeService: KnowledgeService) {}

  ngOnInit() { this.load(); }

  load() {
    this.knowledgeService.all().subscribe(a => {
      this.articles = a;
      this.applyFilter();
    });
  }

  applyFilter() {
    this.filtered = this.articles.filter(a => {
      if (this.filterCategory !== 'ALL' && a.category !== this.filterCategory) return false;
      if (this.search) {
        const t = (a.title + ' ' + a.content + ' ' + (a.keywords || '')).toLowerCase();
        return t.includes(this.search.toLowerCase());
      }
      return true;
    });
  }

  create() {
    this.error = '';
    this.knowledgeService.create({
      title: this.newTitle,
      content: this.newContent,
      category: this.newCategory,
      keywords: this.newKeywords
    }).subscribe({
      next: () => {
        this.showCreate = false;
        this.newTitle = '';
        this.newContent = '';
        this.newKeywords = '';
        this.load();
      },
      error: err => this.error = err.error?.error || 'Failed'
    });
  }

  delete(id: number) {
    if (!confirm('Delete this article?')) return;
    this.knowledgeService.delete(id).subscribe(() => this.load());
  }
}
