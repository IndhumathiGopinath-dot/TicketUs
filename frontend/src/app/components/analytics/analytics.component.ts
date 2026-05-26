import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';

@Component({
  selector: 'app-analytics',
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.css']
})
export class AnalyticsComponent implements OnInit {
  data: any = null;
  loading = true;
  error = '';

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.adminService.analytics().subscribe({
      next: d => { this.data = d; this.loading = false; },
      error: err => { this.error = err.error?.error || 'Failed'; this.loading = false; }
    });
  }

  byCategoryEntries(): { key: string; value: number }[] {
    if (!this.data?.byCategory) return [];
    return Object.entries(this.data.byCategory).map(([k, v]) => ({ key: k, value: v as number }));
  }

  byStatusEntries(): { key: string; value: number }[] {
    if (!this.data?.byStatus) return [];
    return Object.entries(this.data.byStatus).map(([k, v]) => ({ key: k, value: v as number }));
  }

  agentLoadEntries(): { key: string; value: number }[] {
    if (!this.data?.agentWorkload) return [];
    return Object.entries(this.data.agentWorkload).map(([k, v]) => ({ key: k, value: v as number }));
  }

  maxFor(entries: { key: string; value: number }[]): number {
    return Math.max(1, ...entries.map(e => e.value));
  }

  satRatio(): number {
    if (!this.data?.satisfaction) return 0;
    const up = this.data.satisfaction.up || 0;
    const down = this.data.satisfaction.down || 0;
    const total = up + down;
    return total === 0 ? 0 : Math.round((up / total) * 100);
  }
}
