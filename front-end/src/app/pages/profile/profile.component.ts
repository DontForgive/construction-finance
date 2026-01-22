import {Component, OnInit} from '@angular/core';
import Swal from 'sweetalert2';
import {ProfileService} from './profile.service';
import {ToastService} from 'app/utils/toastr';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ImagesService} from '../images/images.service';
import {environment} from '../../../environments/environment';
import { ImageCroppedEvent, ImageTransform } from 'ngx-image-cropper';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  avatarPreviewUrl: string | null = null;
  bannerPreviewUrl: string | null = null;
  private readonly API = `${environment.API_NO_BAR}`

  isCropperOpen = false;
  imageChangedEvent: Event | null = null;
  croppedImageBase64: string | null = null;
  imageFile: File | null = null;

  zoom = 0.1;
  transform: ImageTransform = { scale: 1 };

  constructor(
    private service: ProfileService,
    private toast: ToastService,
    private fb: FormBuilder
  ) {
  }

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      id: [''],
      username: [''],
      fullName: [''],
      email: [''],
      phoneNumber: [''],
      profilePictureUrl: [''],
      bannerUrl: ['']
    });

    this.getProfile();
  }

  onSubmit() {
    this.updateProfile();
  }

  private uploadAndSetUrl(file: File, fieldName: 'profilePictureUrl' | 'bannerUrl'): void {
    Swal.fire({
      title: 'Enviando imagem...',
      text: 'Por favor, aguarde.',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading()
    });

    const request$ =
      fieldName === 'profilePictureUrl'
        ? this.service.updateProfilePicture(file)
        : this.service.updateBanner(file);

    request$.subscribe({
      next: (res: any) => {
        Swal.close();

        const user = res?.data;

        if (fieldName === 'profilePictureUrl') {
          const url = user?.profilePictureUrl ? (this.API + user.profilePictureUrl) : null;
          if (url) {
            this.profileForm.patchValue({ profilePictureUrl: user.profilePictureUrl });
            this.avatarPreviewUrl = url;
          }
        } else {
          const url = user?.bannerUrl ? (this.API + user.bannerUrl) : null;
          if (url) {
            this.profileForm.patchValue({ bannerUrl: user.bannerUrl });
            this.bannerPreviewUrl = url;
          }
        }

        this.service.refreshProfile(); // atualiza navbar e quem estiver escutando
        this.toast.success(res?.message || 'Imagem atualizada com sucesso!');
      },
      error: (err) => {
        Swal.close();
        this.toast.error(err?.error?.message || 'Erro ao enviar imagem.');
      }
    });
  }


  onSelectAvatar(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.imageFile = file;
    this.imageChangedEvent = null;
    this.croppedImageBase64 = null;

    this.zoom = 1;
    this.transform = { scale: 1 };

    this.isCropperOpen = true;
    input.value = '';
  }

  onZoomChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    const v = Number(value);
    const safe = Number.isFinite(v) ? v : 1;

    this.zoom = safe;
    this.transform = { ...this.transform, scale: this.zoom };
  }


  onSelectBanner(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.bannerPreviewUrl = URL.createObjectURL(file);
    this.uploadAndSetUrl(file, 'bannerUrl');

    input.value = '';
  }

  onImageCropped(event: ImageCroppedEvent): void {
    this.croppedImageBase64 = event.base64 || null;
  }


  cancelCrop(): void {
    this.isCropperOpen = false;
    this.imageChangedEvent = null;
    this.imageFile = null;
    this.croppedImageBase64 = null;

    this.zoom = 1;
    this.transform = { scale: 1 };

  }


  confirmCrop(): void {
    if (!this.croppedImageBase64) {
      this.toast.warning('Ajuste o recorte antes de confirmar.');
      return;
    }

    const file = this.base64ToFile(this.croppedImageBase64, 'avatar.png');

    this.avatarPreviewUrl = this.croppedImageBase64;

    this.isCropperOpen = false;
    this.imageChangedEvent = null;
    this.imageFile = null;

    this.uploadAndSetUrl(file, 'profilePictureUrl');
  }


  private base64ToFile(base64: string, filename: string): File {
    const parts = base64.split(',');
    const header = parts[0] || '';
    const data = parts[1] || '';

    const mimeMatch = header.match(/:(.*?);/);
    const mime = mimeMatch ? mimeMatch[1] : 'image/png';

    const binary = atob(data);
    const len = binary.length;
    const bytes = new Uint8Array(len);

    for (let i = 0; i < len; i++) {
      bytes[i] = binary.charCodeAt(i);
    }

    return new File([bytes], filename, { type: mime });
  }

  updateProfile(): void {
    if (this.profileForm.invalid) {
      this.toast.error('Formulário inválido. Verifique os campos e tente novamente.');
      return;
    }
    const profileData = this.profileForm.value;

    Swal.fire({
      title: 'Atualizando perfil...',
      text: 'Por favor, aguarde.',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading()
    });
    this.service.updateProfile(profileData)
      .subscribe({
        next: (res) => {
          Swal.close();
          if (res.status === 200) {
            this.toast.success(res.message || 'Perfil atualizado com sucesso!');
          } else {
            this.toast.warning(res.message || 'Não foi possível atualizar o perfil.');
          }
        },
        error: (err) => {
          Swal.close();
          this.toast.error(err.error?.message || 'Ocorreu um erro ao atualizar o perfil.');
        }
      });
  }

  getProfile(): void {
    this.service.getProfile().subscribe((res) => {
      const data = res['data'];
      this.profileForm.patchValue({
        id: data.id,
        username: data.username,
        fullName: data.fullName,
        email: data.email,
        phoneNumber: data.phoneNumber,
        profilePictureUrl: data.profilePictureUrl,
        bannerUrl: data.bannerUrl
      });

      this.avatarPreviewUrl = this.API + data.profilePictureUrl || null;
      this.bannerPreviewUrl = this.API + data.bannerUrl || null;
    });
  }


  updatePassword(): void {
    Swal.fire({
      title: 'Alterar Senha',
      html: `
      <div class="text-start">
        <label>Senha Atual</label>
        <input id="swal-password" type="password" class="swal2-input" placeholder="Digite sua senha atual">

        <label>Nova Senha</label>
        <input id="swal-new-password" type="password" class="swal2-input" placeholder="Digite a nova senha">

        <label>Confirmar Nova Senha</label>
        <input id="swal-confirm-password" type="password" class="swal2-input" placeholder="Confirme a nova senha">
      </div>
    `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Atualizar',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const password = (document.getElementById('swal-password') as HTMLInputElement).value;
        const newPassword = (document.getElementById('swal-new-password') as HTMLInputElement).value;
        const confirmNewPassword = (document.getElementById('swal-confirm-password') as HTMLInputElement).value;

        if (!password || !newPassword || !confirmNewPassword) {
          Swal.showValidationMessage('Preencha todos os campos.');
          return false;
        }

        if (newPassword !== confirmNewPassword) {
          Swal.showValidationMessage('As novas senhas não coincidem.');
          return false;
        }

        if (newPassword.length < 6) {
          Swal.showValidationMessage('A nova senha deve ter pelo menos 6 caracteres.');
          return false;
        }

        return {password, newPassword, confirmNewPassword};
      }
    }).then((result) => {
      if (result.isConfirmed && result.value) {
        const {password, newPassword, confirmNewPassword} = result.value;

        Swal.fire({
          title: 'Alterando senha...',
          text: 'Por favor, aguarde.',
          allowOutsideClick: false,
          didOpen: () => Swal.showLoading()
        });

        this.service.updatePassword(password, newPassword, confirmNewPassword)
          .subscribe({
            next: (res) => {
              Swal.close();

              if (res.status === 200) {
                Swal.fire({
                  icon: 'success',
                  title: 'Senha alterada',
                  text: res.message || 'Sua senha foi alterada com sucesso!',
                  confirmButtonText: 'Ok'
                });
              } else if (res.status === 401) {
                Swal.fire({
                  icon: 'error',
                  title: 'Senha incorreta',
                  text: res.message || 'Senha atual incorreta.',
                  confirmButtonText: 'Ok'
                });
              } else {
                Swal.fire({
                  icon: 'warning',
                  title: 'Atenção',
                  text: res.message || 'Não foi possível alterar a senha.',
                  confirmButtonText: 'Ok'
                });
              }
            },
            error: (err) => {
              Swal.close();
              Swal.fire({
                icon: 'error',
                title: 'Erro',
                text: err.error?.message || 'Ocorreu um erro ao alterar a senha.',
                confirmButtonText: 'Fechar'
              });
            }
          });
      }
    });
  }

  protected readonly HTMLInputElement = HTMLInputElement;
}
