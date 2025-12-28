import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ApiResponse, ApiResponseTest } from 'app/utils/response';
import { ToastService } from 'app/utils/toastr';
import { ExpenseService } from './expense.service';
import { Expense } from './expense';
import { Supplier } from '../supplier/supplier';
import { Payer } from '../payer/Payer';
import { SupplierService } from '../supplier/supplier.service';
import { PayerService } from '../payer/payer.service';
import { CategoryService } from '../category/category.service';
import { Category } from '../category/category';
import { ServiceContractDTO } from '../serviceContract/service-contract.dto';
import { ServiceContractService } from '../serviceContract/serviceContract.service';

@Component({
  selector: 'app-expense-add-dialog',
  templateUrl: './dialog-component.html',
})
export class ExpenseAddDialogComponent implements OnInit {
  form!: FormGroup;
  suppliers: Supplier[] = [];
  payers: Payer[] = [];
  categories: Category[] = [];
  serviceContract: ServiceContractDTO[] = [];
  selectedFile: File | null = null;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ExpenseAddDialogComponent>,
    private service: ExpenseService,
    private supplierService: SupplierService,
    private categoryService: CategoryService,
    private serviceContractService: ServiceContractService,

    private payerService: PayerService,
    private toast: ToastService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    this.loadSuppliers();
    this.loadCategories();
    this.loadPayers();
    this.loadContractServices();

    this.suppliers = this.data?.supplierId || this.loadSuppliers();
    this.payers = this.data?.payerId || this.loadPayers();
    this.categories = this.data?.categoryId || this.loadCategories();
    this.serviceContract = this.data?.serviceContractId || this.loadContractServices();


    this.form = this.fb.group({
      description: [
        this.data?.description || '',
        [Validators.required, Validators.maxLength(255)],
      ],
      // amount: [this.data?.amount || null, []],
      amount: [
        this.data?.amount ? this.data.amount.toFixed(2).replace('.', ',') : '',
        [
          Validators.required,
          Validators.pattern(/^\d+([,]\d{1,2})?$/), // aceita '50', '50,5', '50,50'
        ],
      ],

      date: [this.data?.date || null, Validators.required],
      supplierId: [this.data?.supplierId || null, Validators.required],
      payerId: [this.data?.payerId || null, Validators.required],
      categoryId: [this.data?.categoryId || null, Validators.required],
      serviceContractId: [this.data?.serviceContractId || null],
      paymentMethod: [this.data?.paymentMethod || null, Validators.required],
      attachmentUrl: [this.data?.attachmentUrl || ''],
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const rawAmount = this.form.value.amount; // Ex: '50,50'

    const parsedAmount =
      typeof rawAmount === 'string'
        ? parseFloat(rawAmount.replace(',', '.'))
        : rawAmount;

    const expenseData: Expense = {
      ...this.form.value,
      amount: parsedAmount,
    };

    if (this.data?.id) {
      // Update expense
      this.service.updateExpense(this.data.id, expenseData).subscribe({
        next: (res) => {
          this.toast.success('Despesa atualizada com sucesso!');
          if (this.selectedFile) {
            this.uploadAttachment(this.data.id);
          } else {
            this.dialogRef.close(res.data);
          }
        },
        error: (err) => {
          this.toast.error('Erro ao atualizar despesa', 'Erro');
          console.error(err);
        },
      });
    } else {
      // Create expense
      this.service.createExpense(expenseData).subscribe({
        next: (res: ApiResponseTest<Expense>) => {
          this.toast.success('Despesa criada com sucesso!');
          this.dialogRef.close(res.data);
          if (this.selectedFile) {
            this.uploadAttachment(res.data.id!); // ✅ agora reconhece id
          } else {
            this.dialogRef.close(res.data);
          }
        },
        error: (err) => {
          this.toast.error('Erro ao criar despesa', 'Erro');
          console.error(err);
        },
      });
    }
  }

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
      },
    });
  }

  loadPayers() {
    this.payerService.getPayers(0, 100).subscribe({
      next: (res) => (this.payers = res.data.content),
      error: (err) => console.error('Erro ao carregar pagadores:', err),
    });
  }

  loadCategories() {
    this.categoryService.getCategories(0, 100, 'id', 'ASC').subscribe({
      next: (res) => {
        this.categories = res.data.content;
      },
      error: (err) => {
        this.toast.error('Erro ao carregar categorias', 'Erro');
        console.error(err);
      },
    });
  }

  loadSuppliers() {
    this.supplierService.getSuppliers(0, 100).subscribe({
      next: (res) => (this.suppliers = res.data.content),
      error: (err) => console.error('Erro ao carregar fornecedores:', err),
    });
  }

  loadContractServices(){
    this.serviceContractService.getServiceContracts().subscribe({
      next: (res) => {
        this.serviceContract = res.data.content;
      },
      error: (err) => {
        this.toast.error('Erro ao carregar contratos de serviço', 'Erro');
        console.error('Erro ao carregar contratos de serviço:', err);
      }
    });
  }

}
