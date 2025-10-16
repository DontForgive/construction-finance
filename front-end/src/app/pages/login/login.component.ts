import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from './auth.service';
import Swal from 'sweetalert2';

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


  resetPassword() {
    Swal.fire({
      title: 'Redefinir senha',
      html: `
      <p>Informe o seu e-mail cadastrado para enviarmos o link de redefinição de senha.</p>
      <input id="swal-email" class="swal2-input" type="email" placeholder="Digite seu e-mail" />
    `,
      showCancelButton: true,
      confirmButtonText: 'Enviar',
      cancelButtonText: 'Cancelar',
      focusConfirm: false,
      preConfirm: () => {
        const email = (document.getElementById('swal-email') as HTMLInputElement).value.trim();
        if (!email) {
          Swal.showValidationMessage('Por favor, digite seu e-mail.');
          return false;
        }
        return email;
      }
    }).then((result) => {
      if (result.isConfirmed && result.value) {
        const email = result.value;

        Swal.fire({
          title: 'Enviando...',
          text: 'Estamos enviando o link de redefinição para seu e-mail.',
          didOpen: () => Swal.showLoading(),
          allowOutsideClick: false,
          allowEscapeKey: false,
          showConfirmButton: false,
        });

        this.authService.resetPassword(email).subscribe({
          next: () => {
            Swal.fire({
              icon: 'success',
              title: 'E-mail enviado!',
              text: 'As instruções para redefinir sua senha foram enviadas para o seu e-mail.',
              confirmButtonText: 'OK',
            });
          },
          error: (err) => {
            console.error('Erro ao enviar e-mail:', err);
            let errorMessage = 'Não foi possível enviar o e-mail de redefinição. Verifique o e-mail informado ou tente novamente mais tarde.';

            try {
              if (err?.error?.erros && Array.isArray(err.error.erros) && err.error.erros.length > 0) {
                errorMessage = err.error.erros[0];
              } else if (typeof err.error === 'string') {
                errorMessage = err.error;
              } else if (err?.message) {
                errorMessage = err.message;
              }
            } catch (e) {
              console.warn('Erro ao processar mensagem de erro:', e);
            }
            Swal.fire({
              icon: 'error',
              title: 'Erro ao enviar',
              text: errorMessage,
              confirmButtonText: 'Fechar'
            });
          }
        });
      }
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
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Usuário ou senha inválidos', 'Erro de login');
        this.loading = false;
      }
    });
  }


}
