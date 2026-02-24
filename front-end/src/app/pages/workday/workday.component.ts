import {Component, OnInit} from '@angular/core';
import {WorkDay, WorkDayPaymentDTO} from './workday';
import {Supplier} from '../supplier/supplier';
import {WorkdayService} from './workday.service';
import {SupplierService} from '../supplier/supplier.service';
import Swal from 'sweetalert2';
import {WorkdayAddDialogComponent} from './WorkdayAddDialogComponent';
import {MatDialog} from '@angular/material/dialog';
import { Payer } from '../payer/Payer';
import { Category } from '../category/category';
import { ServiceContractDTO } from '../serviceContract/service-contract.dto';
import { CategoryService } from '../category/category.service';
import { ServiceContractService } from '../serviceContract/serviceContract.service';
import { PayerService } from '../payer/payer.service';
import { ToastService } from 'app/utils/toastr';
import {WorkdayBulkPaymentDTO} from './WorkdayBulkPaymentDTO';

@Component({
  selector: 'app-workday',
  templateUrl: './workday.component.html',
  styleUrls: ['./workday.component.scss']
})
export class WorkdayComponent implements OnInit {

  workDays: WorkDay[] = [];
  suppliers: Supplier[] = [];
  payers: Payer[] = [];
  categories: Category[] = [];
  serviceContracts: ServiceContractDTO[] = [];


  year = new Date().getFullYear();
  month = new Date().getMonth() + 1;
  supplierId?: number;

  loading = false;

  selectedWorkdays: number[] = [];
  selectedSupplierId?: number;

  constructor(
    private workdayService: WorkdayService,
    private supplierService: SupplierService,
    private categoryService: CategoryService,
    private serviceContractService: ServiceContractService,
    private payerService: PayerService,
    private dialog: MatDialog,
    private toast: ToastService

  ) {
  }

  ngOnInit() {
    this.loadSuppliers();
    this.loadCategories();
    this.loadPayers();
    this.loadContractServices();
    this.loadWorkDays();
  }

  loadContractServices() {
    this.serviceContractService.getServiceContracts().subscribe({
      next: (res) => {
        this.serviceContracts = res.data.content;
      },
      error: (err) => {
        console.error("Erro ao carregar contratos de serviço:", err);
      }
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

   loadPayers() {
    this.payerService.getPayers(0, 100).subscribe({
      next: (res) => (this.payers = res.data.content),
      error: (err) => console.error("Erro ao carregar pagadores:", err),
    });
  }


  toggleSelection(workdayId: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const checked = input.checked;

    const workday = this.workDays.find(w => w.id === workdayId);

    if (!workday || !workday.id) {
      input.checked = false;
      return;
    }

    if (checked) {
      const supplierId = workday.supplierId;

      if (this.selectedSupplierId !== undefined && this.selectedSupplierId !== supplierId) {
        input.checked = false;
        Swal.fire('Atenção', 'Selecione apenas dias do mesmo fornecedor para pagamento em massa.', 'warning');
        return;
      }

      this.selectedSupplierId = supplierId;

      if (!this.selectedWorkdays.includes(workday.id)) {
        this.selectedWorkdays = [...this.selectedWorkdays, workday.id];
      }
    } else {
      this.selectedWorkdays = this.selectedWorkdays.filter(id => id !== workday.id);

      if (this.selectedWorkdays.length === 0) {
        this.selectedSupplierId = undefined;
      }
    }
  }

  private buildBulkPaymentDTO(params: {
    workdayIds: number[];
    supplierId: number;
    amount: number;
    description: string;
    paymentDate: string;
    payerId: number;
    categoryId: number;
    serviceContractId: number;
    paymentMethod: string;
  }): WorkdayBulkPaymentDTO {
    return {
      workdayIds: params.workdayIds,
      supplierId: params.supplierId,
      amount: params.amount,

      description: params.description.trim(),
      paymentDate: params.paymentDate,

      payerId: params.payerId,
      categoryId: params.categoryId,
      serviceContractId: params.serviceContractId,
      paymentMethod: params.paymentMethod as any
    };
  }



  payMultiple(): void {
    if (this.selectedWorkdays.length === 0) {
      return;
    }

    this.openPayDialog(this.selectedWorkdays);
  }

  private openPayDialog(workdayIds: number[], initialValues?: any): void {
    const selected = this.workDays.filter(w => w.id && workdayIds.includes(w.id));
    const supplierId = selected[0]?.supplierId;

    if (!supplierId) {
      Swal.fire('Erro', 'Não foi possível identificar o fornecedor.', 'error');
      return;
    }

    const supplier = this.suppliers.find(s => s.id === supplierId);
    const supplierLabel = supplier ? `${supplier.name}` : `Fornecedor #${supplierId}`;

    const amount = selected.reduce((sum, w) => sum + (w.dailyValue || 0), 0);
    const amountLabel = amount.toFixed(2).replace('.', ',');

    const todayISO = initialValues?.paymentDate || new Date().toISOString().slice(0, 10);

    const payerOptions = this.payers
      .map(p => `<option value="${p.id}" ${initialValues?.payerId == p.id ? 'selected' : ''}>${this.escapeHtml(p.name)}</option>`)
      .join('');

    const categoryOptions = this.categories
      .map(c => `<option value="${c.id}" ${initialValues?.categoryId == c.id ? 'selected' : ''}>${this.escapeHtml(c.name)}</option>`)
      .join('');

    const contractOptions = this.serviceContracts
      .map(c => `<option value="${c.id}" ${initialValues?.serviceContractId == c.id ? 'selected' : ''}>${this.escapeHtml(c.name)}</option>`)
      .join('');

    Swal.fire({
      icon: 'info',
      title: 'Registrar pagamento em massa',
      width: 'min(980px, 94vw)',
      padding: '1.1rem',
      showCancelButton: true,
      confirmButtonText: 'Confirmar pagamento',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      focusConfirm: false,
      showLoaderOnConfirm: true,
      customClass: {
        popup: 'swal-bulk-pay swal2-popup',
        title: 'swal-bulk-pay__title',
        htmlContainer: 'swal-bulk-pay__content',
        actions: 'swal-bulk-pay__actions',
        confirmButton: 'swal-bulk-pay__confirm btn btn-success',
        cancelButton: 'swal-bulk-pay__cancel btn btn-outline-secondary'
      },
      html: `
        <div class="swal-bulk-pay__wrap text-start">
          <aside class="swal-bulk-pay__summary">
            <div class="swal-bulk-pay__summaryTitle">Resumo</div>

            <div class="swal-bulk-pay__summaryRow">
              <span class="text-muted">Dias selecionados</span>
              <strong>${workdayIds.length}</strong>
            </div>

            <div class="swal-bulk-pay__summaryRow">
              <span class="text-muted">Fornecedor</span>
              <strong title="${this.escapeHtml(supplierLabel)}">${this.escapeHtml(supplierLabel)}</strong>
            </div>

            <div class="swal-bulk-pay__summaryRow">
              <span class="text-muted">Total estimado</span>
              <strong>R$ ${amountLabel}</strong>
            </div>

            <div class="swal-bulk-pay__summaryHint">
              Revise os dados e preencha os campos obrigatórios para registrar o pagamento.
            </div>
          </aside>

          <section class="swal-bulk-pay__form">
            <div class="row g-2">
              <div class="col-12">
                <label class="form-label mb-1" for="swal-descricao">Descrição</label>
                <textarea
                  id="swal-descricao"
                  class="form-control"
                  rows="3"
                  maxlength="255"
                  placeholder="Ex.: Pagamento de diárias, ajuste, adiantamento..."
                >${this.escapeHtml(initialValues?.description || '')}</textarea>
                <div class="d-flex justify-content-between">
                  <div class="form-text text-danger">Obrigatório</div>
                  <div class="form-text" id="swal-desc-counter">0/255</div>
                </div>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label mb-1" for="swal-data">Data do pagamento</label>
                <input id="swal-data" type="date" class="form-control" value="${todayISO}">
                <div class="form-text text-danger">Obrigatório</div>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label mb-1" for="swal-method">Método de pagamento</label>
                <select id="swal-method" class="form-select">
                  <option value="">Selecione…</option>
                  <option value="PIX" ${initialValues?.paymentMethod === 'PIX' ? 'selected' : ''}>PIX</option>
                  <option value="BOLETO" ${initialValues?.paymentMethod === 'BOLETO' ? 'selected' : ''}>Boleto</option>
                  <option value="TRANSFERENCIA" ${initialValues?.paymentMethod === 'TRANSFERENCIA' ? 'selected' : ''}>Transferência</option>
                  <option value="CARTAO" ${initialValues?.paymentMethod === 'CARTAO' ? 'selected' : ''}>Cartão</option>
                </select>
                <div class="form-text text-danger">Obrigatório</div>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label mb-1" for="swal-payer">Pagador</label>
                <select id="swal-payer" class="form-select">
                  <option value="">Selecione…</option>
                  ${payerOptions}
                </select>
                <div class="form-text text-danger">Obrigatório</div>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label mb-1" for="swal-category">Categoria</label>
                <select id="swal-category" class="form-select">
                  <option value="">Selecione…</option>
                  ${categoryOptions}
                </select>
                <div class="form-text text-danger">Obrigatório</div>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label mb-1" for="swal-contract">Contrato de serviço</label>
                <select id="swal-contract" class="form-select">
                  <option value="">(Opcional)</option>
                  ${contractOptions}
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label mb-1" for="swal-amount">Valor (R$)</label>
                <input
                  id="swal-amount"
                  type="text"
                  inputmode="decimal"
                  class="form-control"
                  value="${amountLabel}"
                >
                <div class="form-text">Sugestão automática pelo total estimado.</div>
              </div>

              <div class="col-12">
                <label class="form-label mb-1" for="swal-file">Anexo (opcional)</label>
                <input id="swal-file" type="file" class="form-control">
              </div>
            </div>
          </section>
        </div>
      `,
      didOpen: () => {
        const popup = Swal.getPopup();
        if (!popup) return;

        const descEl = popup.querySelector('#swal-descricao') as HTMLTextAreaElement | null;
        const counterEl = popup.querySelector('#swal-desc-counter') as HTMLElement | null;
        const payerEl = popup.querySelector('#swal-payer') as HTMLSelectElement | null;
        const dateEl = popup.querySelector('#swal-data') as HTMLInputElement | null;

        (descEl ?? payerEl ?? dateEl)?.focus();

        const refreshCounter = () => {
          if (!descEl || !counterEl) return;
          counterEl.textContent = `${descEl.value.length}/255`;
        };
        descEl?.addEventListener('input', refreshCounter);
        refreshCounter();

        popup.addEventListener('keydown', (ev: KeyboardEvent) => {
          if (ev.key !== 'Enter') return;
          const target = ev.target as HTMLElement | null;
          const isTextarea = target?.tagName?.toLowerCase() === 'textarea';
          if (isTextarea) return;
          Swal.clickConfirm();
        });
      },
      preConfirm: () => {
        const popup = Swal.getPopup();
        if (!popup) return false;

        const descriptionRaw = (popup.querySelector('#swal-descricao') as HTMLTextAreaElement | null)?.value ?? '';
        const paymentDate = (popup.querySelector('#swal-data') as HTMLInputElement | null)?.value ?? '';
        const payerIdRaw = (popup.querySelector('#swal-payer') as HTMLSelectElement | null)?.value ?? '';
        const categoryIdRaw = (popup.querySelector('#swal-category') as HTMLSelectElement | null)?.value ?? '';
        const serviceContractIdRaw = (popup.querySelector('#swal-contract') as HTMLSelectElement | null)?.value ?? '';
        const paymentMethod = (popup.querySelector('#swal-method') as HTMLSelectElement | null)?.value ?? '';

        const fileInput = popup.querySelector('#swal-file') as HTMLInputElement | null;
        const file = fileInput?.files && fileInput.files.length > 0 ? fileInput.files[0] : null;

        const description = descriptionRaw.trim();
        const payerId = Number(payerIdRaw);
        const categoryId = Number(categoryIdRaw);
        const serviceContractId = serviceContractIdRaw ? Number(serviceContractIdRaw) : 0;

        const errors: string[] = [];
        if (!description) errors.push('Informe a descrição do pagamento.');
        if (!paymentDate) errors.push('Informe a data do pagamento.');
        if (!payerIdRaw || Number.isNaN(payerId) || payerId <= 0) errors.push('Selecione um pagador.');
        if (!categoryIdRaw || Number.isNaN(categoryId) || categoryId <= 0) errors.push('Selecione uma categoria.');
        if (!paymentMethod) errors.push('Selecione o método de pagamento.');

        if (errors.length) {
          Swal.showValidationMessage(`
            <div class="text-start">
              <strong>Revise os campos:</strong>
              <ul class="mb-0 mt-1">
                ${errors.map(e => `<li>${this.escapeHtml(e)}</li>`).join('')}
              </ul>
            </div>
          `);
          return false;
        }

        return {
          description,
          paymentDate,
          payerId,
          categoryId,
          serviceContractId,
          paymentMethod,
          file
        };
      }
    }).then(result => {
      if (!result.isConfirmed || !result.value) {
        return;
      }

      const dto = this.buildBulkPaymentDTO({
        workdayIds,
        supplierId,
        amount,
        description: result.value.description,
        paymentDate: result.value.paymentDate,
        payerId: result.value.payerId,
        categoryId: result.value.categoryId,
        serviceContractId: result.value.serviceContractId,
        paymentMethod: result.value.paymentMethod
      });

      Swal.fire({
        title: 'Processando...',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
      });

      this.workdayService.payBulk(dto, result.value.file).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: 'Pagamento registrado',
            text: 'O pagamento em massa foi enviado com sucesso.',
            showConfirmButton: false,
            timer: 1100,
            timerProgressBar: true
          }).then(() => {
            this.selectedWorkdays = [];
            this.selectedSupplierId = undefined;
            this.loadWorkDays();
          });
        },
        error: (err) => {
          console.error(err);
          Swal.fire({
            icon: 'error',
            title: 'Erro',
            text: 'Não foi possível registrar o pagamento em massa.',
            showCancelButton: true,
            confirmButtonText: 'Revisar dados',
            cancelButtonText: 'Fechar'
          }).then((errResult) => {
            if (errResult.isConfirmed) {
              this.openPayDialog(workdayIds, result.value);
            }
          });
        }
      });
    });
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
      error: (err) => console.error('Erro ao carregar fornecedores:', err),
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
    if (!this.workDays) {
      return 0;
    }
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

        return {date, supplierId, hoursWorked, dailyValue, note};
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

  pay(id: number, initialValues?: any): void {
    const supplierOptions = this.suppliers
      .map(s => `<option value="${s.id}" ${initialValues?.supplierId == s.id ? 'selected' : ''}>${this.escapeHtml(s.name)}</option>`)
      .join('');

    const todayISO = initialValues?.paymentDate || new Date().toISOString().slice(0, 10);

    Swal.fire({
      icon: 'info',
      title: 'Registrar pagamento',
      width: 'min(760px, 92vw)',
      padding: '1.1rem',
      showCancelButton: true,
      confirmButtonText: 'Confirmar',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
      focusConfirm: false,
      showLoaderOnConfirm: true,
      customClass: {
        popup: 'swal-pay swal2-popup',
        title: 'swal-pay__title',
        htmlContainer: 'swal-pay__content',
        actions: 'swal-pay__actions',
        confirmButton: 'swal-pay__confirm btn btn-success',
        cancelButton: 'swal-pay__cancel btn btn-outline-secondary'
      },
      html: `
        <div class="swal-pay__wrap text-start">
          <div class="swal-pay__summary">
            <div class="swal-pay__summaryRow">
              <span class="text-muted mt-3">Registro</span>
              <strong>#${id}</strong>
            </div>
            <div class="swal-pay__summaryHint">
              Preencha os dados abaixo para registrar o pagamento com segurança.
            </div>
          </div>

          <div class="swal-pay__form mt-3">
            <div class="row g-2">
              <div class="col-12 col-md-6">
                <label class="form-label mb-1" for="swal-fornecedor">Fornecedor</label>
                <select id="swal-fornecedor" class="form-select">
                  <option value="">Selecione…</option>
                  ${supplierOptions}
                </select>
                <div class="form-text">Obrigatório</div>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label mb-1" for="swal-data">Data do pagamento</label>
                <input id="swal-data" type="date" class="form-control" value="${todayISO}">
                <div class="form-text">Obrigatório</div>
              </div>

              <div class="col-12">
                <label class="form-label mb-1" for="swal-descricao">Descrição</label>
                <textarea
                  id="swal-descricao"
                  class="form-control"
                  rows="3"
                  maxlength="255"
                  placeholder="Ex.: Diária do dia 10/02, ajuste de horas, adiantamento..."
                >${this.escapeHtml(initialValues?.description || '')}</textarea>
                <div class="d-flex justify-content-between">
                  <div class="form-text">Obrigatório</div>
                  <div class="form-text" id="swal-desc-counter">0/255</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      `,
      didOpen: () => {
        const popup = Swal.getPopup();
        if (!popup) return;

        const supplierEl = popup.querySelector('#swal-fornecedor') as HTMLSelectElement | null;
        const dateEl = popup.querySelector('#swal-data') as HTMLInputElement | null;
        const descEl = popup.querySelector('#swal-descricao') as HTMLTextAreaElement | null;
        const counterEl = popup.querySelector('#swal-desc-counter') as HTMLElement | null;

        // Foco inicial (melhor que jogar no confirm)
        (supplierEl ?? dateEl ?? descEl)?.focus();

        // Contador de caracteres (sensação de produto bem acabado)
        const refreshCounter = () => {
          if (!descEl || !counterEl) return;
          counterEl.textContent = `${descEl.value.length}/255`;
        };
        descEl?.addEventListener('input', refreshCounter);
        refreshCounter();

        // Pequena melhoria: Enter confirma somente se estiver tudo ok
        popup.addEventListener('keydown', (ev: KeyboardEvent) => {
          if (ev.key !== 'Enter') return;
          const target = ev.target as HTMLElement | null;
          const isTextarea = target?.tagName?.toLowerCase() === 'textarea';
          if (isTextarea) return;

          // Deixa o SweetAlert decidir (vai chamar preConfirm)
          Swal.clickConfirm();
        });
      },
      preConfirm: () => {
        const popup = Swal.getPopup();
        if (!popup) return false;

        const supplierIdRaw = (popup.querySelector('#swal-fornecedor') as HTMLSelectElement | null)?.value ?? '';
        const descriptionRaw = (popup.querySelector('#swal-descricao') as HTMLTextAreaElement | null)?.value ?? '';
        const paymentDate = (popup.querySelector('#swal-data') as HTMLInputElement | null)?.value ?? '';

        const supplierId = Number(supplierIdRaw);
        const description = descriptionRaw.trim();

        const errors: string[] = [];
        if (!supplierIdRaw || Number.isNaN(supplierId) || supplierId <= 0) errors.push('Selecione um fornecedor.');
        if (!paymentDate) errors.push('Informe a data do pagamento.');
        if (!description) errors.push('Informe a descrição do pagamento.');

        if (errors.length) {
          Swal.showValidationMessage(`
            <div class="text-start">
              <strong>Revise os campos:</strong>
              <ul class="mb-0 mt-1">
                ${errors.map(e => `<li>${this.escapeHtml(e)}</li>`).join('')}
              </ul>
            </div>
          `);
          return false;
        }

        return {
          supplierId,
          description,
          paymentDate
        };
      }
    }).then(result => {
      if (!result.isConfirmed || !result.value) return;

      const dto: WorkDayPaymentDTO = {
        workdayIds: [id],
        supplierId: result.value.supplierId,
        description: result.value.description,
        paymentDate: result.value.paymentDate
      };

      this.workdayService.pay(dto).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: 'Pagamento registrado',
            text: 'O registro foi marcado como pago.',
            showConfirmButton: false,
            timer: 1100,
            timerProgressBar: true
          }).then(() => this.loadWorkDays());
        },
        error: () => {
          Swal.fire({
            icon: 'error',
            title: 'Não foi possível registrar',
            text: 'Tente novamente. Se o problema persistir, verifique sua conexão ou contate o suporte.',
            showCancelButton: true,
            confirmButtonText: 'Revisar dados',
            cancelButtonText: 'Fechar'
          }).then((errResult) => {
            if (errResult.isConfirmed) {
              this.pay(id, result.value);
            }
          });
        }
      });
    });
  }

  private escapeHtml(value: string): string {
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
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
