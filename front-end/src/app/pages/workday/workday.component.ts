import { Component, OnInit } from '@angular/core';
import { WorkDay } from './workday';
import { Supplier } from '../supplier/supplier';
import { WorkdayService } from './workday.service';
import { SupplierService } from '../supplier/supplier.service';
import Swal from 'sweetalert2';
import { WorkdayAddDialogComponent } from './WorkdayAddDialogComponent';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-workday',
  templateUrl: './workday.component.html',
  styleUrls: ['./workday.component.scss']
})
export class WorkdayComponent implements OnInit {

  workDays: WorkDay[] = [];
  suppliers: Supplier[] = [];

  year = new Date().getFullYear();
  month = new Date().getMonth() + 1;
  supplierId?: number;

  loading = false;

  constructor(
    private workdayService: WorkdayService,
    private supplierService: SupplierService,
    private dialog: MatDialog
  ) { }

  ngOnInit() {
    this.loadSuppliers();
    this.loadWorkDays();
  }


  loadSuppliers() {
    this.supplierService.getSuppliers(0, 100).subscribe({
      next: (res) => (this.suppliers = res.data.content),
      error: (err) => console.error("Erro ao carregar fornecedores:", err),
    });
  }



  loadWorkDays(): void {
    this.loading = true;
    this.workdayService.list(this.year, this.month, this.supplierId).subscribe({
      next: (res) => {
        this.workDays = res.data || [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        console.error(err);
        Swal.fire('Erro', 'Falha ao carregar registros', 'error');
      }
    });
  }

  getTotal(status?: string): number {
    if (!this.workDays) return 0;
    if (status) {
      return this.workDays
        .filter(w => w.status === status)
        .reduce((sum, w) => sum + (w.dailyValue || 0), 0);
    }
    return this.workDays.reduce((sum, w) => sum + (w.dailyValue || 0), 0);
  }


  create(): void {
    Swal.fire({
      title: 'Novo dia de trabalho',
      html: `
        <div class="text-start">
          <label>Data</label>
          <input id="date" type="date" class="swal2-input" style="width:100%">
          <label>Fornecedor</label>
          <select id="supplierId" class="swal2-input" style="width:100%">
            ${this.suppliers.map(s => `<option value="${s.id}">${s.name}</option>`).join('')}
          </select>
          <label>Horas trabalhadas (opcional)</label>
          <input id="hoursWorked" type="number" class="swal2-input" placeholder="8">
          <label>Valor do dia</label>
          <input id="dailyValue" type="number" class="swal2-input" placeholder="150.00">
          <label>Observação</label>
          <input id="note" type="text" class="swal2-input" placeholder="Ex: chuva, meio período">
        </div>
      `,
      confirmButtonText: 'Salvar',
      showCancelButton: true,
      preConfirm: () => {
        const date = (document.getElementById('date') as HTMLInputElement).value;
        const supplierId = Number((document.getElementById('supplierId') as HTMLSelectElement).value);
        const hoursWorked = Number((document.getElementById('hoursWorked') as HTMLInputElement).value);
        const dailyValue = Number((document.getElementById('dailyValue') as HTMLInputElement).value);
        const note = (document.getElementById('note') as HTMLInputElement).value;

        if (!date || !supplierId || !dailyValue) {
          Swal.showValidationMessage('Data, fornecedor e valor são obrigatórios');
          return false;
        }

        return { date, supplierId, hoursWorked, dailyValue, note };
      }
    }).then(result => {
      if (result.isConfirmed && result.value) {
        this.workdayService.create(result.value).subscribe({
          next: () => {
            Swal.fire('Sucesso', 'Registro criado com sucesso!', 'success');
            this.loadWorkDays();
          },
          error: () => Swal.fire('Erro', 'Falha ao criar registro', 'error')
        });
      }
    });
  }

  delete(id: number): void {
    Swal.fire({
      title: 'Excluir registro?',
      text: 'Essa ação não pode ser desfeita.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sim, excluir'
    }).then(result => {
      if (result.isConfirmed) {
        this.workdayService.delete(id).subscribe({
          next: () => {
            Swal.fire('Removido', 'Registro excluído com sucesso', 'success');
            this.loadWorkDays();
          },
          error: () => Swal.fire('Erro', 'Falha ao excluir registro', 'error')
        });
      }
    });
  }

  onMonthChange(event: any): void {
    const [year, month] = event.target.value.split('-').map(Number);
    this.year = year;
    this.month = month;
    this.loadWorkDays();
  }

  onSupplierChange(): void {
    this.loadWorkDays();
  }

  openDialog(): void {
    const dialogRef = this.dialog.open(WorkdayAddDialogComponent, {
      width: '600px',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadWorkDays();
      }
    });

  }
  clearFilters() {
    this.year = new Date().getFullYear();
    this.month = new Date().getMonth() + 1;
    this.supplierId = undefined;
    this.loadWorkDays();
  }
}