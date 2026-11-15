import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ToastService } from 'app/utils/toastr';
import { SupplierComponent } from './supplier.component';
import { SupplierService } from './supplier.service';
import { Supplier } from './supplier';

/** DS1 (completo) dos casos de teste aprovados da HU-1.1. */
const DS1: Supplier = {
  id: 1,
  name: 'Fornecedor Teste Alfa',
  keyPix: 'chave-ficticia-01',
  worker: true,
  zipCode: '13010000',
  street: 'Rua das Flores',
  number: '120',
  complement: 'sala 3',
  neighborhood: 'Centro',
  city: 'Campinas',
  state: 'SP',
  country: 'BR',
  reference: 'ref-01'
};

/** DS4 (vazio): só nome, como em findOrCreateByName. */
const DS4: Supplier = { id: 4, name: 'Fornecedor Criado Pelo Nome', keyPix: '', worker: false };

/** DS5 (longo): logradouro de 200 e complemento de 140 caracteres sintéticos. */
const DS5: Supplier = { ...DS1, id: 5, street: 'R'.repeat(200), complement: 'C'.repeat(140) };

describe('SupplierComponent - coluna Endereço (casos HU-1.1)', () => {
  let fixture: ComponentFixture<SupplierComponent>;
  let component: SupplierComponent;
  let service: jasmine.SpyObj<SupplierService>;

  function carregarComFornecedores(lista: Supplier[]) {
    service.getSuppliers.and.returnValue(of({
      status: 200,
      message: 'Lista de Fornecedores',
      data: { content: lista, totalElements: lista.length, totalPages: 1 }
    } as any));
    fixture.detectChanges();
  }

  function linhas(): HTMLTableRowElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('tbody tr')) as HTMLTableRowElement[];
  }

  beforeEach(waitForAsync(() => {
    service = jasmine.createSpyObj<SupplierService>('SupplierService', ['getSuppliers', 'deleteSupplier']);
    TestBed.configureTestingModule({
      declarations: [SupplierComponent],
      providers: [
        { provide: SupplierService, useValue: service },
        { provide: MatDialog, useValue: jasmine.createSpyObj('MatDialog', ['open']) },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['error', 'success']) }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SupplierComponent);
    component = fixture.componentInstance;
  });

  it('CT-1.1.11 mostra o cabeçalho na ordem #, Nome, Chave PIX, Endereço, Prestador de Serviço, Ações', () => {
    carregarComFornecedores([DS1]);

    const cabecalho = Array.from(fixture.nativeElement.querySelectorAll('thead th'))
      .map((th: any) => th.textContent.trim());

    expect(cabecalho).toEqual(['#', 'Nome', 'Chave PIX', 'Endereço', 'Prestador de Serviço', 'Ações']);
  });

  it('CT-1.1.11 mostra na linha as duas partes do endereço composto (cidade/UF e logradouro · bairro · CEP)', () => {
    carregarComFornecedores([DS1]);

    const celula = linhas()[0].querySelectorAll('td')[3] as HTMLElement;
    expect(celula.querySelector('.endereco-principal')?.textContent?.trim()).toBe('Campinas/SP');
    expect(celula.querySelector('.endereco-secundario')?.textContent?.trim())
      .toBe('Rua das Flores, 120, sala 3 · Centro · 13010-000');
  });

  it('CT-1.1.17 mantém a coluna Chave PIX com o mesmo valor em claro, antes da coluna Endereço', () => {
    carregarComFornecedores([DS1]);

    const celulas = linhas()[0].querySelectorAll('td');
    expect(celulas[2].textContent?.trim()).toBe('chave-ficticia-01');
    expect(celulas[3].classList).toContain('endereco-celula');
  });

  it('CT-1.1.8 e CT-1.1.12 deixa a coluna Endereço em branco para fornecedor sem endereço, sem erro na tela', () => {
    carregarComFornecedores([DS4]);

    const celula = linhas()[0].querySelectorAll('td')[3] as HTMLElement;
    expect(celula.textContent?.trim()).toBe('');
    expect(celula.querySelector('.endereco-principal')).toBeNull();
    expect(celula.querySelector('.endereco-secundario')).toBeNull();
  });

  it('CT-1.1.18 coloca o texto integral do endereço no atributo title da célula', () => {
    carregarComFornecedores([DS5]);

    const celula = linhas()[0].querySelectorAll('td')[3] as HTMLElement;
    expect(celula.getAttribute('title')).toBe(component.addressTitle(DS5));
    expect(celula.getAttribute('title')).toContain('Campinas/SP');
    expect(celula.getAttribute('title')).toContain('R'.repeat(200));
    expect(celula.getAttribute('title')).toContain('C'.repeat(140));
  });

  it('CT-1.1.20 usa o texto "Endereço" no cabeçalho da coluna', () => {
    carregarComFornecedores([DS1]);

    const cabecalhos = Array.from(fixture.nativeElement.querySelectorAll('thead th'))
      .map((th: any) => th.textContent.trim());
    expect(cabecalhos).toContain('Endereço');
  });

  it('CT-1.1.13 (parcial) consulta GET com ordenação id DESC e os filtros digitados, sem alterar o fluxo', () => {
    service.getSuppliers.and.returnValue(of({ data: { content: [], totalElements: 0, totalPages: 0 } } as any));
    component.filterName = 'Alfa';
    component.filterKeyPix = 'chave';
    component.filterWorker = true;

    component.listSuppliers(0);

    expect(service.getSuppliers).toHaveBeenCalledWith(0, component.pageSize, 'id', 'DESC', 'Alfa', 'chave', true);
  });

  it('CT-1.1.14 (parcial, serviço simulado) troca de página pede a página seguinte e mostra o endereço de cada linha, sem repetir itens', () => {
    const PAGINA_1: Supplier[] = [DS1];
    const PAGINA_2: Supplier[] = [DS5];
    service.getSuppliers.and.callFake((page: number) => of({
      status: 200,
      message: 'Lista de Fornecedores',
      data: { content: page === 0 ? PAGINA_1 : PAGINA_2, totalElements: 2, totalPages: 2 }
    } as any));
    fixture.detectChanges();

    component.onPageChange({ pageIndex: 1, pageSize: 5 });
    fixture.detectChanges();

    expect(service.getSuppliers).toHaveBeenCalledWith(1, 5, 'id', 'DESC', '', '', '');
    const idsExibidos = linhas().map(tr => tr.querySelectorAll('td')[0].textContent?.trim());
    expect(idsExibidos).toEqual(['5']);
    const celula = linhas()[0].querySelectorAll('td')[3] as HTMLElement;
    expect(celula.querySelector('.endereco-principal')?.textContent?.trim()).toBe('Campinas/SP');
  });

  it('CT-1.1.15 (parcial, serviço simulado) após editar, a tela recarrega e mostra o número salvo na linha', () => {
    const dialog = TestBed.inject(MatDialog) as jasmine.SpyObj<MatDialog>;
    dialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    carregarComFornecedores([DS1]);

    const DS1_EDITADO: Supplier = { ...DS1, number: '130' };
    service.getSuppliers.and.returnValue(of({
      status: 200,
      message: 'Lista de Fornecedores',
      data: { content: [DS1_EDITADO], totalElements: 1, totalPages: 1 }
    } as any));

    component.openEditDialog(DS1);
    fixture.detectChanges();

    const celula = linhas()[0].querySelectorAll('td')[3] as HTMLElement;
    expect(celula.querySelector('.endereco-secundario')?.textContent?.trim())
      .toBe('Rua das Flores, 130, sala 3 · Centro · 13010-000');
  });

  it('CT-1.1.15 (parcial, serviço simulado) após criar, a tela recarrega e mostra o novo fornecedor com endereço', () => {
    const dialog = TestBed.inject(MatDialog) as jasmine.SpyObj<MatDialog>;
    dialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    carregarComFornecedores([]);

    service.getSuppliers.and.returnValue(of({
      status: 200,
      message: 'Lista de Fornecedores',
      data: { content: [DS1], totalElements: 1, totalPages: 1 }
    } as any));

    component.openAddDialog();
    fixture.detectChanges();

    expect(linhas().length).toBe(1);
    const celula = linhas()[0].querySelectorAll('td')[3] as HTMLElement;
    expect(celula.querySelector('.endereco-principal')?.textContent?.trim()).toBe('Campinas/SP');
  });
});
