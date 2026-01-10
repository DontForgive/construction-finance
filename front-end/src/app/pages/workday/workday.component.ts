import { Component, OnInit } from '@angular/core';
import { WorkDay, WorkDayPaymentDTO } from './workday';
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

  selectedWorkdays: number[] = [];

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
    const page = 0;
    const size = 100;
    const sort = '';
    const dir = '';
    const name = '';
    const worker = true;

    this.supplierService.getSuppliers(page, size, sort, dir, name, worker).subscribe({
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
            Swal.fire({
              icon: 'success',
              title: 'Sucesso',
              text: 'Registro criado com sucesso!',
              showConfirmButton: false,
              timer: 1000,
              timerProgressBar: true
            }).then(() => {
              this.loadWorkDays();
            });
          },
          error: () => Swal.fire('Erro', 'Falha ao criar registro', 'error')
        });
      }
    });
  }

  pay(id: number): void {
    const supplierOptions = this.suppliers
      .map(s => `<option value="${s.id}">${s.name}</option>`)
      .join('');

    Swal.fire({
      title: 'Registrar pagamento',
      html: `
      <div class="text-start">
        <label class="form-label mt-2">Fornecedor:</label>
        <select id="swal-fornecedor" class="form-select">
          <option value="">Selecione</option>
          ${supplierOptions}
        </select>

        <label class="form-label mt-2">Descrição:</label>
        <textarea id="swal-descricao" class="form-control" rows="2" placeholder="Descreva o pagamento"></textarea>

        <label class="form-label mt-2">Data do pagamento:</label>
        <input id="swal-data" type="date" class="form-control">
      </div>
    `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Confirmar pagamento',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const supplierId = (document.getElementById('swal-fornecedor') as HTMLSelectElement).value;
        const description = (document.getElementById('swal-descricao') as HTMLTextAreaElement).value.trim();
        const paymentDate = (document.getElementById('swal-data') as HTMLInputElement).value;

        if (!supplierId || !description || !paymentDate) {
          Swal.showValidationMessage('Preencha todos os campos antes de confirmar');
          return false;
        }

        return { supplierId: Number(supplierId), description, paymentDate };
      }
    }).then(result => {
      if (result.isConfirmed && result.value) {
        const { supplierId, description, paymentDate } = result.value;

        const dto: WorkDayPaymentDTO = {
          workdayIds: [id], // ou this.selectedWorkdays para múltiplos
          supplierId,
          description,
          paymentDate
        };

        this.workdayService.pay(dto).subscribe({
          next: () => {
            Swal.fire({
              icon: 'success',
              title: 'Pago',
              text: 'Registro marcado como pago!',
              showConfirmButton: false,
              timer: 1000,
              timerProgressBar: true
            }).then(() => this.loadWorkDays());
          },
          error: () =>
            Swal.fire('Erro', 'Falha ao registrar o pagamento', 'error')
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
            Swal.fire({
              icon: 'success',
              title: 'Removido',
              text: 'Registro excluído com sucesso!',
              showConfirmButton: false,
              timer: 1000,
              timerProgressBar: true
            }).then(() => {
              this.loadWorkDays();
            });
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
