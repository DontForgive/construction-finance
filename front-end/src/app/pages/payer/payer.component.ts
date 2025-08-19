import { Component, OnInit } from '@angular/core';
import { PayerService } from './payer.service';
import { MatDialog } from '@angular/material/dialog';
import { ToastService } from 'app/utils/toastr';
import { PayerAddDialogComponent } from './payer-add-dialog.component';
import { Payer } from './Payer';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-payer',
  templateUrl: './payer.component.html',
  styleUrls: ['./payer.component.scss']
})
export class PayerComponent implements OnInit {

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 5;
  pageSizes: number[] = [5, 10, 20, 50];

  //filters
  filterName: string = '';

  constructor(private service: PayerService, private dialog: MatDialog, private toast: ToastService) { }

  public list_payers: Payer[] = [];

  ngOnInit() {
    this.listPayers();
  }

  listPayers(page: number = 0) {
    this.currentPage = page;

    this.service.getPayers(
      this.currentPage,
      this.pageSize,
      'id',
      'DESC',
      this.filterName
    ).subscribe(
      (res) => {
        this.list_payers = res.data.content;
        this.totalElements = res.data.totalElements;
        this.totalPages = res.data.totalPages;
      },
      (error) => {
        this.toast.error('Erro ao buscar pagadores', 'Erro');
        console.error('Erro ao buscar pagadores:', error);
      }
    );
  }

  clearFilters() {
    this.filterName = '';
    this.listPayers(0);
  }

  onPageChange(event: any) {
    this.pageSize = event.pageSize;
    this.listPayers(event.pageIndex);
  }

  openAddDialog() {
    const dialogRef = this.dialog.open(PayerAddDialogComponent, {
      width: '500px',
      data: {}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.listPayers(0); // Recarrega a lista após adicionar um novo pagador
      }
    });
  }

  openEditDialog(payer: Payer) {
    const dialogRef = this.dialog.open(PayerAddDialogComponent, {
      width: '500px',
      data: payer
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.listPayers(this.currentPage);
      }
    });
  }

  deletePayer(payer: Payer) {
    Swal.fire({
      title: 'Tem certeza?',
      text: `A categoria "${payer.name}" será excluída!`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sim, excluir',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.service.deletePayer(payer.id).subscribe({
          next: () => {
            Swal.fire({
              icon: 'success',
              title: 'Excluído!',
              text: 'O Pagador foi removido com sucesso.',
              showConfirmButton: false,
              timer: 1000
            });
            this.listPayers(this.currentPage);
          },
          error: (err) => {
            console.error('Erro ao excluir o pagador:', err);
            Swal.fire('Erro!', 'Não foi possível excluir o pagador.', 'error');
          }
        });
      }
    });
  }



}
