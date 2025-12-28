import { Component, OnInit } from '@angular/core';
import { ServiceContractService } from './serviceContract.service';
import { CategoryService } from '../category/category.service';
import { SupplierService } from '../supplier/supplier.service';
import { ToastService } from 'app/utils/toastr';
import Swal from 'sweetalert2';
import { ExpenseService } from '../expense/expense.service';
import { ServiceContractDialogComponent } from './serviceContract-dialog-components';
import { ServiceContractDTO } from './service-contract.dto';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-serviceContract',
  templateUrl: './serviceContract.component.html',
  styleUrls: ['./serviceContract.component.scss']
})
export class ServiceContractComponent implements OnInit {

  constructor(private service: ServiceContractService,
    private serviceCategory: CategoryService,
    private serviceSupplier: SupplierService,
    private serviceExpense: ExpenseService,
    private toast: ToastService,
    private dialog: MatDialog,

  ) { }

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 5;
  pageSizes: number[] = [5, 10, 20, 50];

  filterName: string = '';
  filterDescription: string = '';
  filterSupplierId: number | null = null;
  filterCategoryId: number | null = null;
  filterStartDate: Date | null = null;
  filterEndDate: Date | null = null;
  categories = [];
  suppliers = [];
  list_serviceContracts = [];

  clearFilters() {
    // Lógica para limpar os filtros
    this.filterName = '';
    this.filterDescription = '';
    this.filterSupplierId = null;
    this.filterCategoryId = null;
    this.filterStartDate = null;
    this.filterEndDate = null;
    this.getContracts();
  }

  ngOnInit() {
    this.getContracts();
    this.getCategories();
    this.getSuppliers();
  }


  getContracts(page: number = 0): void {
    this.currentPage = page;

    this.service.getServiceContracts(
      this.currentPage,
      this.pageSize,
      'id',
      'DESC',
      {
        name: this.filterName,
        description: this.filterDescription,
        supplierId: this.filterSupplierId || undefined,
        categoryId: this.filterCategoryId || undefined,
        startDate: this.filterStartDate || undefined,
        endDate: this.filterEndDate || undefined
      }
    ).subscribe({
      next: (response) => {
        console.log('Service Contracts:', response);

        this.list_serviceContracts = response.data.content.map((contract: any) => ({
          ...contract,
          expanded: false,                 // controle da setinha
          payments: contract.payments || [] // garante array
        }));

        this.currentPage = response.data.number;
        this.totalPages = response.data.totalPages;
        this.totalElements = response.data.totalElements;

        this.list_serviceContracts.forEach(contract => {
          console.log(
            'Payments for contract',
            contract.id,
            ':',
            contract.payments
          );
        });
      },
      error: (error) => {
        this.toast.error('Erro ao buscar contratos de serviço', 'Erro');
        console.error('Erro ao buscar contratos de serviço:', error);
      }
    });
  }


  togglePayments(contract: any): void {
    contract.expanded = !contract.expanded;
  }


  getCategories() {
    // Lógica para obter categorias
    this.serviceCategory.getCategories().subscribe(response => {
      this.categories = response.data.content;
    });
  }

  getSuppliers() {
    // Lógica para obter fornecedores
    this.serviceSupplier.getSuppliers().subscribe(response => {
      this.suppliers = response.data.content;
    });
  }

  onPageChange(event: any) {
    this.pageSize = event.pageSize;
    this.getContracts(event.pageIndex);
  }

  openAddDialog() {

    const dialogRef = this.dialog.open(ServiceContractDialogComponent, {
        width: '600px',
        data: {},
      });
      dialogRef.afterClosed().subscribe((result: ServiceContractDTO) => {
        if (result) {
          this.getContracts();
        }
      });

  }

  openEditDialog(serviceContract: ServiceContractDTO) {

    const dialogRef = this.dialog.open(ServiceContractDialogComponent, {
        width: '600px',
        data: { ...serviceContract },
      });
      dialogRef.afterClosed().subscribe((result: ServiceContractDTO) => {
        if (result) {
          this.getContracts();
        }
      });

  }

  deleteServiceContract(id: number) {
    Swal.fire({
      title: 'Confirma a exclusão deste contrato de serviço?',
      text: 'Esta ação não poderá ser desfeita.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sim, excluir',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.service.deleteServiceContract(id).subscribe(
          () => {
            Swal.fire({
              title: 'Excluído!',
              text: 'O contrato de serviço foi excluído com sucesso.',
              icon: 'success',
              showConfirmButton: false,
              timer: 1000
            });
            this.getContracts(this.currentPage);
          },
          (error) => {
            this.toast.error('Erro ao excluir o contrato de serviço.', 'Erro');
            console.error('Erro ao excluir o contrato de serviço:', error);
          }
        );
      }
    });
  }

   generateReceipt(expenseId: number): void {
    this.serviceExpense.generateAndOpenReceipt(expenseId);
  }

}
