import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ApiResponse } from 'app/utils/response';
import { ToastService } from 'app/utils/toastr';
import { Payer } from './Payer';
import { PayerService } from './payer.service';

@Component({
  selector: 'app-payer-add-dialog',
  templateUrl: './dialog-component.html'
})

export class PayerAddDialogComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<PayerAddDialogComponent>,
    private service: PayerService,
    private toast: ToastService,

    @Inject(MAT_DIALOG_DATA) public data: any

  ) {
    this.form = this.fb.group({
      name: [data?.name || '', [Validators.required, Validators.maxLength(120)]]
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payer = this.form.value;


    if (this.data?.id) {
      this.service.updatePayer(this.data.id, payer).subscribe({
        next: (res: ApiResponse<Payer>) => {
          this.toast.success(
            'Pagador atualizado com sucesso!');
          this.dialogRef.close(res.data);
        },
        error: (err) => {
          console.error('Erro ao atualizar o pagador:', err);
          this.toast.error('Erro ao atualizar o pagador.', 'Erro');
        }
      });
    } else {
      this.service.createPayer(payer).subscribe({
        next: (res: ApiResponse<Payer>) => {
          this.toast.success(
            'Pagador criado com sucesso!',
          );
          this.dialogRef.close(res.data);
        },
        error: (err) => {
          console.error('Erro ao criar o pagador:', err);
          this.toast.error('Erro ao criar pagador.', 'Erro');
        }
      });
    }

  }

}