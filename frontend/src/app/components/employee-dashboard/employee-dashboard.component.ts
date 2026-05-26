import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TicketService } from '../../services/ticket.service';
import { Ticket, Status } from '../../models/models';

@Component({
  selector: 'app-employee-dashboard',
  templateUrl: './employee-dashboard.component.html',
  styleUrls: ['./employee-dashboard.component.css']
})
export class EmployeeDashboardComponent implements OnInit {
  tickets: Ticket[] = [];
  filter: Status | 'ALL' = 'ALL';
  loading = false;
  error = '';

  constructor(private ticketService: TicketService, private router: Router) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.ticketService.list().subscribe({
      next: data => { this.tickets = data; this.loading = false; },
      error: err => { this.error = err.error?.error || 'Failed to load'; this.loading = false; }
    });
  }

  filtered(): Ticket[] {
    if (this.filter === 'ALL') return this.tickets;
    return this.tickets.filter(t => t.status === this.filter);
  }

  open(t: Ticket) { this.router.navigate(['/ticket', t.id]); }

  count(status: Status): number {
    return this.tickets.filter(t => t.status === status).length;
  }
}
