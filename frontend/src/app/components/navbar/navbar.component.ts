import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { User, Notification } from '../../models/models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  user: User | null = null;
  notifications: Notification[] = [];
  unreadCount = 0;
  showDropdown = false;
  private sub?: Subscription;
  private pollSub?: Subscription;

  constructor(
    public auth: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.sub = this.auth.currentUser$.subscribe(u => {
      this.user = u;
      if (u) this.loadNotifications();
    });
    // Poll every 30s
    this.pollSub = interval(30000).subscribe(() => {
      if (this.auth.isLoggedIn()) this.loadNotifications();
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
    this.pollSub?.unsubscribe();
  }

  loadNotifications() {
    this.notificationService.list().subscribe(list => {
      this.notifications = list;
      this.unreadCount = list.filter(n => !n.read).length;
    });
  }

  toggleDropdown() {
    this.showDropdown = !this.showDropdown;
  }

  openNotification(n: Notification) {
    if (!n.read) this.notificationService.markAsRead(n.id).subscribe(() => this.loadNotifications());
    if (n.ticketId) {
      this.router.navigate(['/ticket', n.ticketId]);
      this.showDropdown = false;
    }
  }

  markAllRead() {
    this.notificationService.markAllAsRead().subscribe(() => this.loadNotifications());
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
