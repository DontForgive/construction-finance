import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CategoryService } from './category.service';
import { ToastrService } from 'ngx-toastr';
import { ApiResponse } from 'app/utils/response';
import { Category } from './category';

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

        <mat-form-field appearance="outline" class="w-100">
          <mat-label>Descrição</mat-label>
          <textarea matInput formControlName="description" maxlength="255" rows="3"></textarea>
          <mat-error *ngIf="form.get('description')?.hasError('maxlength')">
            Descrição deve ter no máximo 255 caracteres
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
export class CategoryAddDialogComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<CategoryAddDialogComponent>,
    private categoryService: CategoryService,
    private toastr: ToastrService,
    @Inject(MAT_DIALOG_DATA) public data: any

  ) {
    this.form = this.fb.group({
      name: [data?.name || '', [Validators.required, Validators.maxLength(120)]],
      description: [data?.description || '', [Validators.maxLength(255)]]
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const category = this.form.value;

    if (this.data?.id) {
      this.categoryService.updateCategory(this.data.id, category).subscribe({
        next: (res: ApiResponse<Category>) => {
          this.toastr.success('Categoria atualizada com sucesso!', 'Sucesso');
          this.dialogRef.close(res.data); // agora o TS sabe que existe res.data
        },
        error: (err) => {
          console.error('Erro ao atualizar categoria:', err);
          this.toastr.error('Erro ao atualizar categoria.', 'Erro');
        }
      });
    } else {
      this.categoryService.createCategory(category).subscribe({
        next: (res: ApiResponse<Category>) => {
          this.toastr.success('Categoria criada com sucesso!', 'Sucesso');
          this.dialogRef.close(res.data);
        },
        error: (err) => {
          console.error('Erro ao criar categoria:', err);
          this.toastr.error('Erro ao criar categoria.', 'Erro');
        }
      });
    }

  }




}