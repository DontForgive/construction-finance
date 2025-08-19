import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  form: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.loading = true;
    this.authService.login(this.form.value).subscribe({
      next: (res) => {
        if (res.token) {
          this.authService.saveToken(res.token);
          this.toastr.success('Login realizado com sucesso!', 'Bem-vindo');
          this.router.navigate(['/dashboard']);
        }
        this.loading = false; // garante reset no sucesso
      },
      error: () => {
        this.toastr.error('Usuário ou senha inválidos', 'Erro de login');
        this.loading = false; // ✅ reseta no erro também
      }
    });
  }

}
