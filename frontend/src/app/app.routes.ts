import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { EmployeeDashboardComponent } from './components/employee-dashboard/employee-dashboard.component';
import { CreateTicketComponent } from './components/create-ticket/create-ticket.component';
import { TicketDetailComponent } from './components/ticket-detail/ticket-detail.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { AnalyticsComponent } from './components/analytics/analytics.component';
import { KnowledgeBaseComponent } from './components/knowledge-base/knowledge-base.component';
import { authGuard, adminGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'dashboard', component: EmployeeDashboardComponent, canActivate: [authGuard] },
  { path: 'create', component: CreateTicketComponent, canActivate: [authGuard] },
  { path: 'ticket/:id', component: TicketDetailComponent, canActivate: [authGuard] },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [adminGuard] },
  { path: 'analytics', component: AnalyticsComponent, canActivate: [adminGuard] },
  { path: 'knowledge', component: KnowledgeBaseComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'dashboard' }
];
