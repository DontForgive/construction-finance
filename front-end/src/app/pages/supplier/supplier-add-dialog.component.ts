import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { ApiResponse } from 'app/utils/response';
import { SupplierService } from './supplier.service';
import { Supplier } from './supplier';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-category-add-dialog',
  template: `
    <h2 mat-dialog-title class="mat-headline-6 d-flex align-items-center">
        <mat-icon color="primary" class="me-2">category</mat-icon>
        <span class="text-muted">Adicionar Categoria</span>
    </h2>

    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <mat-dialog-content>
        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>Nome</mat-label>
          <input matInput formControlName="name" maxlength="120" required>
          <mat-error *ngIf="form.get('name')?.hasError('required')">
            Nome é obrigatório
          </mat-error>
          <mat-error *ngIf="form.get('name')?.hasError('maxlength')">
            Nome deve ter no máximo 120 caracteres
          </mat-error>
        </mat-form-field>

      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-stroked-button color="warn" mat-dialog-close type="button">
          <mat-icon class="me-1">close</mat-icon>
          Cancelar
        </button>
        <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
          <mat-icon class="me-1">save</mat-icon>
          Salvar
        </button>
      </mat-dialog-actions>
    </form>
  `
})
export class SupplierAddDialogComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<SupplierAddDialogComponent>,
    private supplierService: SupplierService,
    private toastr: ToastrService,
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

    const supplier = this.form.value;

    if (this.data?.id) {
      this.supplierService.updateSupplier(this.data.id, supplier).subscribe({
        next: (res: ApiResponse<Supplier>) => {
          this.toastr.success('Categoria atualizada com sucesso!', 'Sucesso');
          this.dialogRef.close(res.data); // agora o TS sabe que existe res.data
        },
        error: (err) => {
          Swal.fire({
            icon: 'error',
            title: 'Erro ao atualizar o Fornecedor',
            text: err.error?.message || 'Erro desconhecido',
          });
        }
      });
    } else {
      this.supplierService.createSupplier(supplier).subscribe({
        next: (res: ApiResponse<Supplier>) => {
          this.toastr.success(
            '<span data-notify="icon" class="nc-icon nc-bell-55"></span>' +
            '<span data-notify="message">Fornecedor Criado com sucesso</span>',
            "",
            {
              timeOut: 2000,
              closeButton: true,
              enableHtml: true,
              toastClass: "alert alert-success alert-with-icon",
              positionClass: "toast-top-right"
            }
          );

          this.dialogRef.close(res.data);
        },
        error: (err) => {
          Swal.fire({
            icon: 'error',
            title: 'Erro ao criar Fornecedor',
            text: err.error?.message || 'Erro desconhecido',
          });
        }
      });
    }

  }
}


