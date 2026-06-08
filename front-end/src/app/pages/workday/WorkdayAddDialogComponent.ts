import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import Swal from 'sweetalert2';
import { Supplier } from '../supplier/supplier';
import { WorkdayService } from './workday.service';
import { SupplierService } from '../supplier/supplier.service';

@Component({
  selector: 'app-workday-add-dialog',
  templateUrl: './workday-add-dialog.component.html',
  styleUrls: ['./workday-add-dialog.component.css']
})
export class WorkdayAddDialogComponent implements OnInit {

  form!: FormGroup;
  suppliers: Supplier[] = [];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<WorkdayAddDialogComponent>,
    private workdayService: WorkdayService,
    private supplierService: SupplierService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  ngOnInit(): void {
    let initialDate = null;
    if (this.data?.date) {
      // Se a data vier como string (Ex: 2024-05-20), o mat-datepicker precisa de um objeto Date.
      // Adicionamos 'T00:00:00' para evitar problemas de fuso horário ao criar o objeto Date
      const dateStr = typeof this.data.date === 'string' ? this.data.date.split('T')[0] + 'T00:00:00' : this.data.date;
      initialDate = new Date(dateStr);
    }

    this.form = this.fb.group({
      date: [initialDate, Validators.required],
      supplierId: [this.data?.supplierId || null, Validators.required],
      hoursWorked: [this.data?.hoursWorked || null],
      dailyValue: [this.data?.dailyValue || null, Validators.required],
      note: [this.data?.note || '']
    });

    this.loadSuppliers();
  }

  loadSuppliers() {
    const page = 0;
    const size = 100;
    const sort = '';
    const dir = '';
    const name = '';
    const worker = true;

    this.supplierService.getSuppliers(page, size,sort, dir, name, worker).subscribe({
      next: (res) => (this.suppliers = res.data.content),
      error: (err) => console.error("Erro ao carregar fornecedores:", err),
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const formValue = this.form.value;

    // Formata a data para yyyy-MM-dd antes de enviar para o back-end
    let formattedDate = formValue.date;
    if (formValue.date instanceof Date) {
      formattedDate = formValue.date.toISOString().split('T')[0];
    }

    const dto = {
      ...formValue,
      date: formattedDate
    };

    const request = this.data?.id
      ? this.workdayService.update(this.data.id, dto)
      : this.workdayService.create(dto);

    request.subscribe({
      next: (res) => {
        const message = this.data?.id ? 'Registro alterado com sucesso!' : 'Registro criado com sucesso!';
        Swal.fire({
          icon: 'success',
          title: 'Sucesso',
          text: message,
          showConfirmButton: false,
          timer: 1000,
          timerProgressBar: true
        });
        this.dialogRef.close(true);
      },
      error: (err) => {
        console.error("Erro na requisição:", err);
        const message = this.data?.id ? 'Falha ao alterar registro' : 'Falha ao criar registro';
        Swal.fire('Erro', message, 'error');
      }
    });
  }
}
