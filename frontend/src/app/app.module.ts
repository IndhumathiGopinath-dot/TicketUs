import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { AppComponent } from './app.component';
import { routes } from './app.routes';
import { AuthInterceptor } from './interceptors/auth.interceptor';

import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { EmployeeDashboardComponent } from './components/employee-dashboard/employee-dashboard.component';
import { CreateTicketComponent } from './components/create-ticket/create-ticket.component';
import { TicketDetailComponent } from './components/ticket-detail/ticket-detail.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { AnalyticsComponent } from './components/analytics/analytics.component';
import { KnowledgeBaseComponent } from './components/knowledge-base/knowledge-base.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    SignupComponent,
    NavbarComponent,
    EmployeeDashboardComponent,
    CreateTicketComponent,
    TicketDetailComponent,
    AdminDashboardComponent,
    AnalyticsComponent,
    KnowledgeBaseComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    FormsModule,
    HttpClientModule,
    RouterModule.forRoot(routes)
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
