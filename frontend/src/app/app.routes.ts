import { Routes } from '@angular/router';
import { LandingComponent } from './components/landing/landing.component';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { EmployeeDashboardComponent } from './components/employee-dashboard/employee-dashboard.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { CreateTicketComponent } from './components/create-ticket/create-ticket.component';
import { TicketDetailComponent } from './components/ticket-detail/ticket-detail.component';
import { KnowledgeBaseComponent } from './components/knowledge-base/knowledge-base.component';
import { AnalyticsComponent } from './components/analytics/analytics.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  // Landing page at root
  { path: '', component: LandingComponent },

  // Public routes
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },

  // Protected routes
  { path: 'dashboard', component: EmployeeDashboardComponent, canActivate: [authGuard] },
  { path: 'create', component: CreateTicketComponent, canActivate: [authGuard] },
  { path: 'ticket/:id', component: TicketDetailComponent, canActivate: [authGuard] },
  { path: 'knowledge', component: KnowledgeBaseComponent, canActivate: [authGuard] },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [authGuard] },
  { path: 'analytics', component: AnalyticsComponent, canActivate: [authGuard] },

  // Catch-all
  { path: '**', redirectTo: '' }
];