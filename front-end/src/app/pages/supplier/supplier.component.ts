import { Component, OnInit, OnDestroy } from '@angular/core';
import { SupplierService } from './supplier.service';
import { MatDialog } from '@angular/material/dialog';
import { Supplier } from './supplier';
import { SupplierAddDialogComponent } from './supplier-add-dialog.component';
import Swal from 'sweetalert2';
import { ToastService } from 'app/utils/toastr';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-supplier',
  templateUrl: './supplier.component.html',
  styleUrls: ['./supplier.component.scss']
})
export class SupplierComponent implements OnInit, OnDestroy {

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 5;
  pageSizes: number[] = [5, 10, 20, 50];

  private searchSubject = new Subject<void>();
  private searchSubscription?: Subscription;

  constructor(private service: SupplierService, private dialog: MatDialog,
     private toast: ToastService,
  ) { }

  public list_suppliers: Supplier[] = [];
  filterName: string = '';
  filterKeyPix: string = '';
  filterWorker: any = '';

  ngOnInit() {
    this.listSuppliers();

    this.searchSubscription = this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => {
      this.listSuppliers(0);
    });
  }

  ngOnDestroy() {
    this.searchSubscription?.unsubscribe();
  }

  onFilterChange() {
    this.searchSubject.next();
  }


  onPageChange(event: any) {
    this.pageSize = event.pageSize;
    this.listSuppliers(event.pageIndex);
  }

  listSuppliers(page: number = 0) {
    this.currentPage = page;

    this.service.getSuppliers(
      this.currentPage,
      this.pageSize,
      'id',
      'DESC',
      this.filterName,
      this.filterKeyPix,
      this.filterWorker
    ).subscribe(
      (res) => {
        this.list_suppliers = res.data.content;
        this.totalElements = res.data.totalElements;
        this.totalPages = res.data.totalPages;
      },
      (error) => {
        this.toast.error('Erro ao buscar fornecedores \n' + error.message, "Error");
        console.error('Erro ao buscar fornecedores:', error.message);
      }
    );
  }

  clearFilters() {
    this.filterName = '';
    this.filterKeyPix = '';
    this.filterWorker = '';
    this.listSuppliers(0);
  }


  openAddDialog() {
    const dialogRef = this.dialog.open(SupplierAddDialogComponent, {
      width: '500px'
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.listSuppliers(this.currentPage);
      }
    });
  }

  openEditDialog(supplier: Supplier) {

    const dialogRef = this.dialog.open(SupplierAddDialogComponent, {
      width: '500px',
      data: supplier
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.listSuppliers(this.currentPage);
      }
    });
  }


  deleteSupplier(supplier: Supplier) {
    Swal.fire({
      title: 'Tem certeza?',
      text: `O Fornecedor "${supplier.name}" será excluída!`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sim, excluir',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.service.deleteSupplier(supplier.id).subscribe({
          next: () => {
            Swal.fire({
              icon: 'success',
              title: 'Excluída!',
              text: 'O Fornecedor foi removido com sucesso.',
              showConfirmButton: false,
              timer: 1000
            });
            this.listSuppliers(this.currentPage); // Atualiza tabela
          },
          error: (err) => {
            console.error('Erro ao excluir o Fornecedor:', err);
             Swal.fire({
              icon: 'error',
              title: 'Não foi possível excluir o Fornecedor',
              text: err.error?.message || 'Erro desconhecido',
            });
          }
        });
      }
    });
  }


}
