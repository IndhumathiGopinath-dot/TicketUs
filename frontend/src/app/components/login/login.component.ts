import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.error = '';
    this.loading = true;
    this.auth.login(this.email, this.password).subscribe({
      next: resp => {
        this.loading = false;
        this.router.navigate([resp.role === 'ADMIN' ? '/admin' : '/dashboard']);
      },
      error: err => {
        this.loading = false;
        this.error = err.error?.error || 'Login failed';
      }
    });
  }
}
