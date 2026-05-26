import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TicketService } from '../../services/ticket.service';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { Ticket, TimelineEntry, Status, User } from '../../models/models';

@Component({
  selector: 'app-ticket-detail',
  templateUrl: './ticket-detail.component.html',
  styleUrls: ['./ticket-detail.component.css']
})
export class TicketDetailComponent implements OnInit {
  ticket?: Ticket;
  timeline: TimelineEntry[] = [];
  attachments: any[] = [];
  loading = true;
  error = '';
  message = '';

  user: User | null = null;
  agents: any[] = [];
  newStatus: Status = 'IN_PROGRESS';
  statusNotes = '';
  selectedAgentId?: number;
  selectedFile?: File;

  statusOptions: Status[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private adminService: AdminService,
    public auth: AuthService
  ) {}

  ngOnInit() {
    this.user = this.auth.getCurrentUser();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
    if (this.user?.role === 'ADMIN') {
      this.adminService.agents().subscribe(a => this.agents = a);
    }
  }

  load(id: number) {
    this.loading = true;
    this.ticketService.get(id).subscribe({
      next: t => {
        this.ticket = t;
        this.newStatus = t.status;
        this.selectedAgentId = t.assignedToId;
        this.loading = false;
        this.loadTimeline();
        this.loadAttachments();
      },
      error: err => {
        this.error = err.error?.error || 'Failed to load';
        this.loading = false;
      }
    });
  }

  loadTimeline() {
    if (!this.ticket) return;
    this.ticketService.timeline(this.ticket.id).subscribe(t => this.timeline = t);
  }

  loadAttachments() {
    if (!this.ticket) return;
    this.ticketService.listAttachments(this.ticket.id).subscribe(a => this.attachments = a);
  }

  updateStatus() {
    if (!this.ticket) return;
    this.ticketService.updateStatus(this.ticket.id, this.newStatus, this.statusNotes).subscribe({
      next: t => {
        this.ticket = t;
        this.statusNotes = '';
        this.message = 'Status updated';
        this.loadTimeline();
        setTimeout(() => this.message = '', 2000);
      },
      error: err => this.error = err.error?.error || 'Failed'
    });
  }

  rate(rating: number) {
    if (!this.ticket) return;
    this.ticketService.rate(this.ticket.id, rating).subscribe({
      next: t => { this.ticket = t; this.message = 'Thanks for your feedback!'; },
      error: err => this.error = err.error?.error || 'Failed'
    });
  }

  assign() {
    if (!this.ticket || !this.selectedAgentId) return;
    this.adminService.assign(this.ticket.id, this.selectedAgentId).subscribe({
      next: t => { this.ticket = t; this.message = 'Reassigned'; this.loadTimeline(); },
      error: err => this.error = err.error?.error || 'Failed'
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files?.[0];
  }

  uploadFile() {
    if (!this.ticket || !this.selectedFile) return;
    this.ticketService.uploadAttachment(this.ticket.id, this.selectedFile).subscribe({
      next: () => { this.selectedFile = undefined; this.loadAttachments(); },
      error: err => this.error = err.error?.error || 'Upload failed'
    });
  }

  downloadUrl(p: string) {
    return this.ticketService.downloadUrl(p);
  }

  canEdit(): boolean {
    if (!this.user || !this.ticket) return false;
    return this.user.role === 'ADMIN' ||
           (this.ticket.assignedToId === this.user.id);
  }

  canRate(): boolean {
    if (!this.user || !this.ticket) return false;
    return this.ticket.createdById === this.user.id &&
           (this.ticket.status === 'RESOLVED' || this.ticket.status === 'CLOSED') &&
           !this.ticket.satisfactionRating;
  }

  iconFor(action: string): string {
    switch (action) {
      case 'CREATED': return '✨';
      case 'ASSIGNED': return '👤';
      case 'STATUS_CHANGED': return '🔄';
      case 'ESCALATED': return '⚠';
      case 'RATED': return '⭐';
      default: return '📌';
    }
  }
}
