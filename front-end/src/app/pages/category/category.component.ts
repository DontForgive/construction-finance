import { Component, OnInit, OnDestroy } from '@angular/core';
import { CategoryService } from './category.service';
import { Category } from './category';
import { MatDialog } from '@angular/material/dialog';
import { CategoryAddDialogComponent } from './category-add-dialog.component';
import Swal from 'sweetalert2';
import { ToastService } from 'app/utils/toastr';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss']
})
export class CategoryComponent implements OnInit, OnDestroy {

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 5;
  pageSizes: number[] = [5, 10, 20, 50];

  private searchSubject = new Subject<void>();
  private searchSubscription?: Subscription;

  constructor(private service: CategoryService, private dialog: MatDialog,
     private toast: ToastService,
  ) { }

  public list_categories: Category[] = [];

  ngOnInit() {
    this.listCategories();

    this.searchSubscription = this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => {
      this.listCategories(0);
    });
  }

  ngOnDestroy() {
    this.searchSubscription?.unsubscribe();
  }

  onFilterChange() {
    this.searchSubject.next();
  }

  filterName: string = '';
  filterDescription: string = '';

  listCategories(page: number = 0) {
    this.currentPage = page;

    this.service.getCategories(
      this.currentPage,
      this.pageSize,
      'id',
      'DESC',
      this.filterName,
      this.filterDescription
    ).subscribe(
      (res) => {
        this.list_categories = res.data.content;
        this.totalElements = res.data.totalElements;
        this.totalPages = res.data.totalPages;
      },
      (error) => {
        this.toast.error('Erro ao buscar categorias', 'Erro');
        console.error('Erro ao buscar categorias:', error);
      }
    );
  }

  clearFilters() {
    this.filterName = '';
    this.filterDescription = '';
    this.listCategories(0);
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
      data: category
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.listCategories(this.currentPage);
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
             Swal.fire({
                          icon: 'success',
                          title: 'Excluída!',
                          text: 'A categoria foi removida com sucesso.',
                          showConfirmButton: false,
                          timer: 1000
                        });
            this.listCategories(this.currentPage);
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



