import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TicketService } from '../../services/ticket.service';
import { AdminService } from '../../services/admin.service';
import { Ticket, Status, Category } from '../../models/models';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  tickets: Ticket[] = [];
  users: any[] = [];
  view: 'tickets' | 'users' = 'tickets';
  filterStatus: Status | 'ALL' = 'ALL';
  filterCategory: Category | 'ALL' = 'ALL';
  search = '';
  loading = false;
  error = '';

  constructor(
    private ticketService: TicketService,
    private adminService: AdminService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadTickets();
    this.loadUsers();
  }

  loadTickets() {
    this.loading = true;
    this.ticketService.list().subscribe({
      next: t => { this.tickets = t; this.loading = false; },
      error: err => { this.error = err.error?.error || 'Failed'; this.loading = false; }
    });
  }

  loadUsers() {
    this.adminService.users().subscribe(u => this.users = u);
  }

  filtered(): Ticket[] {
    return this.tickets.filter(t => {
      if (this.filterStatus !== 'ALL' && t.status !== this.filterStatus) return false;
      if (this.filterCategory !== 'ALL' && t.category !== this.filterCategory) return false;
      if (this.search && !t.title.toLowerCase().includes(this.search.toLowerCase())
          && !String(t.id).includes(this.search)) return false;
      return true;
    });
  }

  deleteUser(id: number, name: string) {
    if (!confirm(`Delete user "${name}"?`)) return;
    this.adminService.deleteUser(id).subscribe({
      next: () => this.loadUsers(),
      error: err => this.error = err.error?.error || 'Failed to delete'
    });
  }

  open(t: Ticket) {
    this.router.navigate(['/ticket', t.id]);
  }
}
