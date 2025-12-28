import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ServiceContractService } from './serviceContract.service';
import { SupplierService } from '../supplier/supplier.service';
import { CategoryService } from '../category/category.service';
import { Supplier } from '../supplier/supplier';
import { Category } from '../category/category';
import { ServiceContractDTO } from './service-contract.dto';
import { ApiResponseTest } from 'app/utils/response';
import { ToastService } from 'app/utils/toastr';

@Component({
    selector: 'app-expense-add-dialog',
    templateUrl: './dialog-component.html',
})

export class ServiceContractDialogComponent implements OnInit {

    form!: FormGroup;
    suppliers: Supplier[] = [];
    categories: Category[] = [];


    constructor(
        private fb: FormBuilder,
        private dialogRef: MatDialogRef<ServiceContractDialogComponent>,
        private service: ServiceContractService,
        private serviceSupplier: SupplierService,
        private serviceCategory: CategoryService,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private toast: ToastService,



    ) { }

    ngOnInit(): void {

        this.getSuppliers();
        this.getCategories();

        this.suppliers = this.data?.supplierId || this.getSuppliers();
        this.categories = this.data?.categoryId || this.getCategories();

        this.form = this.fb.group({
            name: [
                this.data?.name || '',
                [Validators.required, Validators.maxLength(255)],
            ],
            description: [
                this.data?.description || '',
                [Validators.required, Validators.maxLength(255)],
            ],
            contractedValue: [
                this.data?.contractedValue ? this.data.contractedValue.toFixed(2).replace('.', ',') : '',
                [
                    Validators.required,
                    Validators.pattern(/^\d+([,]\d{1,2})?$/), // aceita '50', '50,5', '50,50'
                ],
            ],
            startDate: [this.data?.startDate || null, Validators.required],
            endDate: [this.data?.endDate || null, Validators.required],
            supplierId: [this.data?.supplierId || null, Validators.required],
            categoryId: [this.data?.categoryId || null, Validators.required],
        });


    }

    onSubmit() {

      if (this.form.invalid) {
        this.form.markAllAsTouched();
        return;
      }

      const rawAmount = this.form.value.contractedValue; // Ex: '50,50'


      const parsedAmount =
        typeof rawAmount === 'string'
          ? parseFloat(rawAmount.replace(',', '.'))
          : rawAmount;

      const serviceContract: ServiceContractDTO = {
        ...this.form.value,
        contractedValue: parsedAmount,
      };


      if (this.data?.id) {
        this.service.updateServiceContract(this.data.id, serviceContract).subscribe({
          next: (res) => {
            this.toast.success('Contrato atualizado com sucesso!');
            this.dialogRef.close(res.data);
          },
          error: (err) => {
            console.error('Erro ao atualizar:', err);
            this.toast.error('Erro ao atualizar contrato.');
          }
        });
      } else {
            this.service.createServiceContract(serviceContract).subscribe({
                next: (res: ApiResponseTest<ServiceContractDTO>) => {
                    this.toast.success('Contrato de serviço criado com sucesso!');
                    this.dialogRef.close(res.data);
                },
                error: (err) => {
                    this.toast.error('Erro ao criar contrato de serviço.', 'Erro');
                    console.error(err);
                }
            });
        }

    }

    getSuppliers(): void {
        this.serviceSupplier.getSuppliers().subscribe((response) => {
            this.suppliers = response.data.content;
        });
    }

    getCategories(): void {
        this.serviceCategory.getCategories().subscribe((response) => {
            this.categories = response.data.content;
        });
    }
}
