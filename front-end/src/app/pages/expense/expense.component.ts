import { Component, OnInit } from "@angular/core";
import { ExpenseService } from "./expense.service";
import { MatDialog } from "@angular/material/dialog";
import { ToastService } from "app/utils/toastr";
import { Expense } from "./expense";
import { ApiResponse } from "app/utils/response";
import { ExpenseAddDialogComponent } from "./expense-add-dialog.component";
import { Supplier } from "../supplier/supplier";
import { Payer } from "../payer/Payer";
import { SupplierService } from "../supplier/supplier.service";
import { PayerService } from "../payer/payer.service";
import { Category } from "../category/category";
import { CategoryService } from "../category/category.service";
import Swal from "sweetalert2";

@Component({
  selector: "app-expense",
  templateUrl: "./expense.component.html",
  styleUrls: ["./expense.component.scss"],
})
export class ExpenseComponent implements OnInit {
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 5;
  pageSizes: number[] = [5, 10, 20, 50];

  filterDescription: string = "";
  filterSupplierId: number | null = null;
  filterPayerId: number | null = null;
  filterCategoryId: number | null = null;
  filterPaymentMethod: string = "";
  filterDate: string = "";

  public list_expenses: Expense[] = [];
  suppliers: Supplier[] = [];
  payers: Payer[] = [];
  categories: Category[] = [];

  constructor(
    private service: ExpenseService,
    private supplierService: SupplierService,
    private payerService: PayerService,
    private categoryService: CategoryService,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit() {
    this.listExpenses();
    this.loadPayers();
    this.loadSuppliers();
    this.loadCategories();
  }

  /** Lista com filtros e paginação */
  listExpenses(page: number = 0) {
    this.currentPage = page;

    this.service
      .getExpenses(this.currentPage, this.pageSize, "id", "DESC", {
        description: this.filterDescription,
        supplierId: this.filterSupplierId,
        payerId: this.filterPayerId,
        categoryId: this.filterCategoryId,
        paymentMethod: this.filterPaymentMethod,
        date: this.filterDate,
      })
      .subscribe({
        next: (res) => {
          this.list_expenses = res.data.content;
          this.totalElements = res.data.totalElements;
          this.totalPages = res.data.totalPages;
        },
        error: (err) => {
          this.toast.error("Erro ao buscar despesas", "Erro");
          console.error("Erro ao buscar despesas:", err);
        },
      });
  }

  /** Abre o dialog para criar uma nova despesa */
  openAddDialog() {
    const dialogRef = this.dialog.open(ExpenseAddDialogComponent, {
      width: "600px",
      data: {}, // pode passar suppliers/payers se quiser popular selects
    });

    dialogRef.afterClosed().subscribe((result: Expense) => {
      if (result) {
        this.toast.success("Despesa criada com sucesso!");
        this.listExpenses(this.currentPage);
      }
    });
  }

  /** Abre o dialog para editar uma despesa */
  openEditDialog(expense: Expense) {
    const dialogRef = this.dialog.open(ExpenseAddDialogComponent, {
      width: "600px",
      data: expense,
    });

    dialogRef.afterClosed().subscribe((result: Expense) => {
      if (result) {
        this.toast.success("Despesa atualizada com sucesso!");
        this.listExpenses(this.currentPage);
      }
    });
  }

  /** Exclui uma despesa */

  deleteExpense(expense: Expense) {
    Swal.fire({
      title: "Tem certeza?",
      text: `O lançamento "${expense.description}" será excluído!`,
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#d33",
      cancelButtonColor: "#6c757d",
      confirmButtonText: "Sim, excluir",
      cancelButtonText: "Cancelar",
    }).then((result) => {
      if (result.isConfirmed) {
        this.service.deleteExpense(expense.id).subscribe({
          next: () => {
            Swal.fire({
              icon: "success",
              title: "Excluída!",
              text: "O lançamento foi removido com sucesso.",
              showConfirmButton: false,
              timer: 1000,
            });
            this.listExpenses(this.currentPage);
          },
          error: (err) => {
            console.error("Erro ao excluir categoria:", err);
            Swal.fire(
              "Erro!",
              "Não foi possível excluir a categoria.",
              "error"
            );
          },
        });
      }
    });
  }

  /** Limpa os filtros e recarrega */
  clearFilters() {
    this.filterDescription = "";
    this.filterSupplierId = null;
    this.filterPayerId = null;
    this.filterPaymentMethod = "";
    this.filterDate = "";
    this.filterCategoryId = null;
    this.listExpenses(0);
  }

  /** Evento de paginação do MatPaginator */
  onPageChange(event: any) {
    this.pageSize = event.pageSize;
    this.currentPage = event.pageIndex;
    this.listExpenses(this.currentPage);
  }

  loadSuppliers() {
    this.supplierService.getSuppliers(0, 100).subscribe({
      next: (res) => (this.suppliers = res.data.content),
      error: (err) => console.error("Erro ao carregar fornecedores:", err),
    });
  }

  loadPayers() {
    this.payerService.getPayers(0, 100).subscribe({
      next: (res) => (this.payers = res.data.content),
      error: (err) => console.error("Erro ao carregar pagadores:", err),
    });
  }

  loadCategories() {
    this.categoryService.getCategories(0, 100, "id", "ASC").subscribe({
      next: (res) => {
        this.categories = res.data.content;
      },
      error: (err) => {
        this.toast.error("Erro ao carregar categorias", "Erro");
        console.error(err);
      },
    });
  }
}
