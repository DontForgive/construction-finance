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
    this.form = this.fb.group({
      date: [null, Validators.required],
      supplierId: [null, Validators.required],
      hoursWorked: [null],
      dailyValue: [null, Validators.required],
      note: ['']
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

    const dto = this.form.value;
    this.workdayService.create(dto).subscribe({
      next: (res) => {
        Swal.fire('Sucesso', 'Registro criado com sucesso!', 'success');
        this.dialogRef.close(true);
      },
      error: () => Swal.fire('Erro', 'Falha ao criar registro', 'error')
    });
  }
}
