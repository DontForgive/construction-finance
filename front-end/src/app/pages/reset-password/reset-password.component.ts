import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../login/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {
  form: FormGroup;
  loading = false;
  token: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
    this.form = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validator: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');
    if (!this.token) {
      Swal.fire({
        icon: 'error',
        title: 'Token ausente',
        text: 'O link de redefinição de senha é inválido ou está incompleto.',
        confirmButtonText: 'Voltar ao Login'
      }).then(() => {
        this.router.navigate(['/login']);
      });
    }
  }

  passwordMatchValidator(g: FormGroup) {
    const password = g.get('newPassword')?.value;
    const confirmPassword = g.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { mismatch: true };
  }

  onSubmit(): void {
    if (this.form.invalid || !this.token) {
      return;
    }

    this.loading = true;
    const { newPassword } = this.form.value;

    this.authService.confirmResetPassword({ token: this.token, newPassword }).subscribe({
      next: (response) => {
        this.loading = false;
        Swal.fire({
          icon: 'success',
          title: 'Sucesso!',
          text: response.message || 'Senha alterada com sucesso.',
          timer: 3000,
          showConfirmButton: false
        }).then(() => {
          this.router.navigate(['/login']);
        });
      },
      error: (err) => {
        this.loading = false;
        let errorMessage = 'Não foi possível redefinir sua senha. Tente novamente mais tarde.';

        if (err.error && err.error.message) {
          errorMessage = err.error.message;
        } else if (err.error && err.error.erros && err.error.erros.length > 0) {
          errorMessage = err.error.erros[0];
        }

        Swal.fire({
          icon: 'error',
          title: 'Erro',
          text: errorMessage,
          confirmButtonText: 'Fechar'
        });
      }
    });
  }
}
