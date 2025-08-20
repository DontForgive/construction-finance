import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ApiResponse } from 'app/utils/response';
import { ToastService } from 'app/utils/toastr';
import { ExpenseService } from './expense.service';
import { Expense } from './expense';
import { Supplier } from '../supplier/supplier';
import { Payer } from '../payer/Payer';

@Component({
  selector: 'app-expense-add-dialog',
  templateUrl: './dialog-component.html',
})
export class ExpenseAddDialogComponent implements OnInit {
  form!: FormGroup;
  suppliers: Supplier[] = [];
  payers: Payer[] = [];
  selectedFile: File | null = null;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ExpenseAddDialogComponent>,
    private service: ExpenseService,
    private toast: ToastService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    // Se você já passar suppliers e payers no data do MatDialog
    this.suppliers = this.data?.suppliers || [];
    this.payers = this.data?.payers || [];


    this.form = this.fb.group({
      description: [this.data?.description || '', [Validators.required, Validators.maxLength(255)]],
      amount: [this.data?.amount || null, [Validators.required, Validators.min(0.01)]],
      date: [this.data?.date || null, Validators.required],
      supplierId: [this.data?.supplierId || null, Validators.required],
      payerId: [this.data?.payerId || null, Validators.required],
      paymentMethod: [this.data?.paymentMethod || null, Validators.required],
      attachmentUrl: [this.data?.attachmentUrl || '']
    });
  }

    onSubmit() {}
  // onSubmit() {
  //   if (this.form.invalid) {
  //     this.form.markAllAsTouched();
  //     return;
  //   }

  //   const expenseData = this.form.value;

  //   if (this.data?.id) {
  //     // Update expense
  //     this.service.updateExpense(this.data.id, expenseData).subscribe({
  //       next: (res) => {
  //         this.toast.success('Despesa atualizada com sucesso!');
  //         if (this.selectedFile) {
  //           this.uploadAttachment(this.data.id);
  //         } else {
  //           this.dialogRef.close(res.data);
  //         }
  //       },
  //       error: (err) => {
  //         this.toast.error('Erro ao atualizar despesa', 'Erro');
  //         console.error(err);
  //       }
  //     });
  //   } else {
  //     // Create expense
  //     this.service.createExpense(expenseData).subscribe({
  //       next: (res: ApiResponse<Expense>) => {
  //         this.toast.success('Despesa criada com sucesso!');
  //         if (this.selectedFile) {
  //           this.uploadAttachment(res.data.id!); // ✅ agora reconhece id
  //         } else {
  //           this.dialogRef.close(res.data);
  //         }
  //       },
  //       error: (err) => {
  //         this.toast.error('Erro ao criar despesa', 'Erro');
  //         console.error(err);
  //       }
  //     });

  //   }
  // }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  private uploadAttachment(expenseId: number) {
    this.service.uploadAttachment(expenseId, this.selectedFile!).subscribe({
      next: (res) => {
        this.toast.success('Anexo enviado com sucesso!');
        this.dialogRef.close(res.data);
      },
      error: (err) => {
        this.toast.error('Erro ao enviar anexo', 'Erro');
        console.error(err);
      }
    });
  }
}
