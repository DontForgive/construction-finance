import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import Chart from 'chart.js';
import { ExpenseReportService, ChartDataDTO } from './expense-report.service';
import { Supplier } from '../supplier/supplier';
import { Payer } from '../payer/Payer';
import { Category } from '../category/category';
import { SupplierService } from '../supplier/supplier.service';
import { PayerService } from '../payer/payer.service';
import { CategoryService } from '../category/category.service';
import { ToastService } from 'app/utils/toastr';

@Component({
  selector: 'dashboard-cmp',
  moduleId: module.id,
  templateUrl: 'dashboard.component.html'
})
export class DashboardComponent implements OnInit {

  public chartCategory: any;
  public chartPaymentMethod: any;
  public chartMonth: any;
  public chartSupplier: any;
  public chartPayer: any;
  public ctx: any;

  filterForm: FormGroup;

  // KPIs (valores mockados, depois você pode ligar ao backend)
  kpis = [
    { label: 'Total Despesas', value: 'R$ 0,00', icon: 'nc-icon nc-money-coins text-success', footer: 'Total geral de despesas' },
    { label: 'Pagadores', value: '0', icon: 'nc-icon nc-single-02 text-info', footer: 'Usuários ativos' },
    { label: 'Fornecedores', value: '0', icon: 'nc-icon nc-shop text-warning', footer: 'Cadastrados' },
    { label: 'Categorias', value: '0', icon: 'nc-icon nc-tag-content text-danger', footer: 'Categorias de despesa' }
  ];

  // Mock de dados (substituir por chamadas de serviço depois)
  suppliers: Supplier[] = [];
  payers: Payer[] = [];
  categories: Category[] = [];

  constructor(
    private fb: FormBuilder,
    private reportService: ExpenseReportService,
    private supplierService: SupplierService,
    private payerService: PayerService,
    private categoryService: CategoryService,
    private toast: ToastService
  ) {
    this.filterForm = this.fb.group({
      startDate: [null],
      endDate: [null],
      categoryId: [null],
      supplierId: [null],
      payerId: [null]
    });
  }

  ngOnInit() {
    this.loadAllCharts();
    this.loadKpis();
    this.loadPayers();
    this.loadSuppliers();
    this.loadCategories();
  }

  onClear(): void {
    this.filterForm.reset();
    this.loadAllCharts();
    this.loadKpis(); 
  }

  /** Aplica os filtros e recarrega gráficos */
  applyFilters() {
    this.loadAllCharts();
    this.loadKpis(); 
  }

  /** 🔹 Carrega todos os gráficos */
  private loadAllCharts() {
    this.loadCategoryChart();
    this.loadPaymentMethodChart();
    this.loadMonthChart();
    this.loadSupplierChart();
    this.loadPayerChart();
  }

  /** 🔹 Monta filtros */
  private getFilters() {
    const f = this.filterForm.value;
    return {
      start: f.startDate,
      end: f.endDate,
      categoryId: f.categoryId,
      supplierId: f.supplierId,
      payerId: f.payerId
    };
  }

  /** 🔹 KPI mockado (pode puxar de API depois) */
  private loadKpis() {
    this.reportService.getKpis(this.getFilters()).subscribe(data => {
      const formatter = new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
      });

      this.kpis[0].value = formatter.format(data.totalExpenses);
      this.kpis[1].value = data.totalPayers;
      this.kpis[2].value = data.totalSuppliers;
      this.kpis[3].value = data.totalCategories;
    });
  }


  /** 🔹 Gráfico por Categoria */
  private loadCategoryChart() {
    this.reportService.getByCategory(this.getFilters()).subscribe((data: ChartDataDTO[]) => {
      const labels = data.map(d => d.label);
      const values = data.map(d => d.value);

      this.ctx = document.getElementById('chartCategory');
      if (this.chartCategory) this.chartCategory.destroy();

      this.chartCategory = new Chart(this.ctx, {
        type: 'pie',
        data: {
          labels,
          datasets: [{
            backgroundColor: ['#e3e3e3', '#4acccd', '#fcc468', '#ef8157', '#6bd098'],
            borderWidth: 0,
            data: values
          }]
        },
        options: { legend: { display: true } }
      });
    });
  }

  /** 🔹 Gráfico por Método de Pagamento */
  private loadPaymentMethodChart() {
    this.reportService.getByPaymentMethod(this.getFilters()).subscribe((data: ChartDataDTO[]) => {
      const labels = data.map(d => d.label);
      const values = data.map(d => d.value);

      this.ctx = document.getElementById('chartPaymentMethod');
      if (this.chartPaymentMethod) this.chartPaymentMethod.destroy();

      this.chartPaymentMethod = new Chart(this.ctx, {
        type: 'doughnut',
        data: {
          labels,
          datasets: [{
            backgroundColor: ['#51CACF', '#fbc658', '#ef8157', '#6bd098'],
            borderWidth: 0,
            data: values
          }]
        },
        options: { legend: { display: true } }
      });
    });
  }

  /** 🔹 Gráfico por Mês */
  private loadMonthChart() {
  this.reportService.getByMonth(this.getFilters()).subscribe((data: ChartDataDTO[]) => {
    const labels = data.map(d => d.label);
    const values = data.map(d => d.value);

    const canvas = document.getElementById('chartMonth') as HTMLCanvasElement;
    const ctx = canvas.getContext('2d')!;

    if (this.chartMonth) this.chartMonth.destroy();

    // ★ Gradiente bonito
    const gradient = ctx.createLinearGradient(0, 0, 0, 250);
    gradient.addColorStop(0, 'rgba(107, 208, 152, 0.35)');
    gradient.addColorStop(1, 'rgba(107, 208, 152, 0.02)');

    this.chartMonth = new Chart(ctx, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Despesas por mês',
          data: values,
          borderColor: '#41c983',
          borderWidth: 3,
          backgroundColor: gradient,     
          pointBackgroundColor: '#41c983',
          pointBorderColor: '#fff',
          pointRadius: 6,                
          pointHoverRadius: 10,
          tension: 0.35                   
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: true
          },
          tooltip: {
            usePointStyle: true,
            padding: 12
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: { color: 'rgba(0,0,0,0.05)' }
          },
          x: {
            grid: { display: false },
            ticks: { autoSkip: true, maxTicksLimit: 12 }
          }
        }
      }
    });
  });
}


  /** 🔹 Gráfico por Fornecedor */
  private loadSupplierChart() {
    this.reportService.getBySupplier(this.getFilters()).subscribe((data: ChartDataDTO[]) => {
      const labels = data.map(d => d.label);
      const values = data.map(d => d.value);

      this.ctx = document.getElementById('chartSupplier');
      if (this.chartSupplier) this.chartSupplier.destroy();

      this.chartSupplier = new Chart(this.ctx, {
        type: 'bar',
        data: {
          labels,
          datasets: [{
            label: 'Despesas por fornecedor',
            backgroundColor: '#f17e5d',
            data: values
          }]
        },
        options: { legend: { display: false } }
      });
    });
  }

  /** 🔹 Gráfico por Pagador */
  private loadPayerChart() {
    this.reportService.getByPayer(this.getFilters()).subscribe((data: ChartDataDTO[]) => {
      const labels = data.map(d => d.label);
      const values = data.map(d => d.value);

      this.ctx = document.getElementById('chartPayer');
      if (this.chartPayer) this.chartPayer.destroy();

      this.chartPayer = new Chart(this.ctx, {
        type: 'bar',
        data: {
          labels,
          datasets: [{
            label: 'Despesas por pagador',
            backgroundColor: '#fcc468',
            data: values
          }]
        },
        options: { legend: { display: false } }
      });
    });
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
