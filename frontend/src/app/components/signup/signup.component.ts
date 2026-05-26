import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/models';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['../login/login.component.css']
})
export class SignupComponent {
  name = '';
  email = '';
  password = '';
  role: Role = 'EMPLOYEE';
  department = '';
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.error = '';
    this.loading = true;
    this.auth.signup({
      name: this.name,
      email: this.email,
      password: this.password,
      role: this.role,
      department: this.department
    }).subscribe({
      next: resp => {
        this.loading = false;
        this.router.navigate([resp.role === 'ADMIN' ? '/admin' : '/dashboard']);
      },
      error: err => {
        this.loading = false;
        this.error = err.error?.error || 'Signup failed';
      }
    });
  }
}
