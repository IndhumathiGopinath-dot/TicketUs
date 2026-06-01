import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {
    if (this.auth.isLoggedIn()) {
      this.router.navigate([this.auth.isAdmin() ? '/admin' : '/dashboard']);
    }
  }

  helpItems = [
    {
      num: '01',
      title: 'Raise',
      text: 'Create tickets in seconds. Category-specific fields for IT, Bugs, and HR — only the inputs that matter for your issue.'
    },
    {
      num: '02',
      title: 'Route',
      text: 'Auto-priority detection from your description. Outages route to URGENT, simple requests stay LOW — instantly.'
    },
    {
      num: '03',
      title: 'Resolve',
      text: 'Track every status change with a full timeline. Rate the resolution. Build a knowledge base from what gets solved.'
    }
  ];

  recentWork = [
    { name: 'Email server outage',  meta: 'IT, Urgent',  year: '/26' },
    { name: 'Mobile layout broken', meta: 'Bug, Normal', year: '/26' },
    { name: 'Payroll discrepancy',  meta: 'HR, Urgent',  year: '/26' },
    { name: 'Leave request',        meta: 'HR, Low',     year: '/26' }
  ];

  process = [
    { num: '01', title: 'Sign up in 30 seconds' },
    { num: '02', title: 'Raise your first ticket' },
    { num: '03', title: 'Get auto-routed to the right team' },
    { num: '04', title: 'Track to resolution and rate' }
  ];
}