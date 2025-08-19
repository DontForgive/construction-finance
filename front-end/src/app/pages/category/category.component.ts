import { Component, OnInit } from '@angular/core';
import { CategoryService } from './category.service';
import { Category } from './category';
import { MatDialog } from '@angular/material/dialog';
import { CategoryAddDialogComponent } from './category-add-dialog.component';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss']
})
export class CategoryComponent implements OnInit {

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 10;
  pageSizes: number[] = [3, 5, 10, 20, 50];

  constructor(private service: CategoryService, private dialog: MatDialog,
    private toastr: ToastrService
  ) { }

  public list_categories: Category[] = [];

  ngOnInit() {
    this.listCategories();
  }


  listCategories(page: number = 0) {
    this.currentPage = page;
    this.service.getCategories(this.currentPage, this.pageSize, 'id', 'DESC').subscribe(
      (res) => {

        console.log('API Response:', res); // Adicione esta linha
        this.list_categories = res.data.content;
        this.totalElements = res.data.totalElements;
        this.totalPages = res.data.totalPages;
      },
      (error) => {
        console.error('Erro ao buscar categorias:', error);
      }
    );
  }

  onPageChange(event: any) {
    this.pageSize = event.pageSize;
    this.listCategories(event.pageIndex);
  }

  openAddDialog() {
    const dialogRef = this.dialog.open(CategoryAddDialogComponent, {
      width: '500px'
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.listCategories(this.currentPage);
      }
    });
  }

  openEditDialog(category: Category) {
    const dialogRef = this.dialog.open(CategoryAddDialogComponent, {
      width: '500px',
      data: category // passa os dados da categoria
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Chama o service de update passando o id e os dados editados
        this.service.updateCategory(category.id, result).subscribe({
          next: () => {
            this.toastr.success('Categoria atualizada com sucesso!', 'Sucesso');
            this.listCategories(this.currentPage); // Atualiza tabela
          },
          error: (err) => {
            console.error('Erro ao atualizar categoria:', err);
            this.toastr.error('Erro ao atualizar categoria', 'Erro');
          }
        });
      }
    });
  }


deleteCategory(category: Category) {
  Swal.fire({
    title: 'Tem certeza?',
    text: `A categoria "${category.name}" será excluída!`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Sim, excluir',
    cancelButtonText: 'Cancelar'
  }).then((result) => {
    if (result.isConfirmed) {
      this.service.deleteCategory(category.id).subscribe({
        next: () => {
          Swal.fire('Excluída!', 'A categoria foi removida com sucesso.', 'success');
          this.listCategories(this.currentPage); // Atualiza tabela
        },
        error: (err) => {
          console.error('Erro ao excluir categoria:', err);
          Swal.fire('Erro!', 'Não foi possível excluir a categoria.', 'error');
        }
      });
    }
  });
}


}



