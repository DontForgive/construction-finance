import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormControl } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import Swal from 'sweetalert2';
import { ToastService } from 'app/utils/toastr';
import { SupplierAddDialogComponent } from './supplier-add-dialog.component';
import { SupplierService } from './supplier.service';
import { PHONE_ERROR_MESSAGE, phoneValidator } from './supplier-phone';
import { cepValidator } from './supplier-address';

/** Os 13 campos do formulário, na ordem em que o formulário os define hoje (baseline antes da HU-1.1 de UX). */
const CAMPOS_NA_ORDEM_ATUAL = [
  'name', 'keyPix', 'phone', 'worker', 'zipCode', 'street', 'number', 'complement',
  'neighborhood', 'city', 'state', 'country', 'reference'
];

/** Fornecedor sintético com endereço completo (CT-1.1.12). */
const FORNECEDOR_COMPLETO = {
  id: 1,
  name: 'Fornecedor Teste Alfa',
  keyPix: 'chave-ficticia-01',
  worker: true,
  phone: '1130000001',
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

describe('SupplierAddDialogComponent - layout e preservação (casos HU-1.1 UX)', () => {
  let fixture: ComponentFixture<SupplierAddDialogComponent>;
  let component: SupplierAddDialogComponent;
  let service: jasmine.SpyObj<SupplierService>;
  let dialogRef: jasmine.SpyObj<any>;
  let dadosDoDialogo: any;

  function abrir(data: any = {}) {
    dadosDoDialogo = data;
    fixture = TestBed.createComponent(SupplierAddDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function host(): HTMLElement {
    return fixture.nativeElement as HTMLElement;
  }

  function campo(nome: string): HTMLElement {
    return host().querySelector(`[formControlName="${nome}"]`) as HTMLElement;
  }

  function nomesDosCampos(raiz: ParentNode): string[] {
    return Array.from(raiz.querySelectorAll('[formControlName]'))
      .map(el => el.getAttribute('formControlName') as string);
  }

  /** Texto próprio do título, sem o texto do ícone (mat-icon). */
  function textoDoTitulo(h3: Element): string {
    return Array.from(h3.childNodes)
      .filter(n => n.nodeType === Node.TEXT_NODE)
      .map(n => n.textContent ?? '')
      .join('')
      .replace(/\s+/g, ' ')
      .trim();
  }

  /** Elemento de coluna (classe col-*) que envolve o campo. */
  function coluna(nome: string): HTMLElement {
    let el: HTMLElement | null = campo(nome);
    while (el && !Array.from(el.classList).some(c => c.startsWith('col-'))) {
      el = el.parentElement;
    }
    return el as HTMLElement;
  }

  function mensagensDeErro(nome: string): string[] {
    const campoFormulario = campo(nome).closest('mat-form-field') as HTMLElement;
    return Array.from(campoFormulario.querySelectorAll('mat-error')).map(e => (e.textContent ?? '').trim());
  }

  beforeEach(waitForAsync(() => {
    service = jasmine.createSpyObj<SupplierService>('SupplierService',
      ['createSupplier', 'updateSupplier', 'getSupplierById']);
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    spyOn(Swal, 'fire').and.returnValue(Promise.resolve({} as any));

    TestBed.configureTestingModule({
      declarations: [SupplierAddDialogComponent],
      imports: [
        NoopAnimationsModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatCheckboxModule,
        MatIconModule,
        MatButtonModule
      ],
      providers: [
        { provide: SupplierService, useValue: service },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useFactory: () => dadosDoDialogo },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['error', 'success']) }
      ]
    }).compileComponents();
  }));

  it('CT-1.1.1 mostra os títulos Dados gerais, Contato e Endereço (opcional) nessa ordem, com cada campo no bloco do seu grupo', () => {
    abrir();

    const titulos = Array.from(host().querySelectorAll('form h3'));
    expect(titulos.map(textoDoTitulo)).toEqual(['Dados gerais', 'Contato', 'Endereço (opcional)']);

    const camposPorGrupo = titulos.map(h3 => nomesDosCampos(h3.parentElement as HTMLElement).sort());
    expect(camposPorGrupo[0]).toEqual(['keyPix', 'name', 'worker']);
    expect(camposPorGrupo[1]).toEqual(['phone']);
    expect(camposPorGrupo[2]).toEqual(
      ['city', 'complement', 'country', 'neighborhood', 'number', 'reference', 'state', 'street', 'zipCode']
    );
  });

  it('CT-1.1.2 renderiza os 13 formControlName exatamente uma vez cada', () => {
    abrir();

    const renderizados = nomesDosCampos(host());
    expect(renderizados.length).toBe(13);
    CAMPOS_NA_ORDEM_ATUAL.forEach(nome => {
      expect(renderizados.filter(n => n === nome).length)
        .withContext(`campo ${nome}`).toBe(1);
    });
  });

  it('CT-1.1.3 mantém no DOM a mesma ordem dos campos definida hoje no formulário (ordem de tabulação)', () => {
    abrir();

    expect(nomesDosCampos(host())).toEqual(CAMPOS_NA_ORDEM_ATUAL);
  });

  it('CT-1.1.4 coloca os pares keyPix|worker, zipCode|state, number|complement e neighborhood|city em um row com col-md-6', () => {
    abrir();

    const pares = [['keyPix', 'worker'], ['zipCode', 'state'], ['number', 'complement'], ['neighborhood', 'city']];
    pares.forEach(([a, b]) => {
      const colA = coluna(a);
      const colB = coluna(b);
      expect(colA.classList).withContext(`${a} col-md-6`).toContain('col-md-6');
      expect(colB.classList).withContext(`${b} col-md-6`).toContain('col-md-6');
      expect(colA.parentElement).withContext(`${a} e ${b} no mesmo row`).toBe(colB.parentElement);
      expect(colA.parentElement!.classList).toContain('row');
    });
  });

  it('CT-1.1.4 mantém name, street e reference em coluna de largura total (col-12, sem col-md-6) e phone e worker com col-12', () => {
    abrir();

    ['name', 'street', 'reference'].forEach(nome => {
      expect(coluna(nome).classList).withContext(`${nome} col-12`).toContain('col-12');
      expect(coluna(nome).classList).withContext(`${nome} sem col-md-6`).not.toContain('col-md-6');
      expect(coluna(nome).parentElement!.classList).toContain('row');
    });
    ['phone', 'worker'].forEach(nome => {
      expect(coluna(nome).classList).withContext(`${nome} col-12`).toContain('col-12');
    });
  });

  it('CT-1.1.5 aceita nome com 120 caracteres e rejeita com 121 (maxlength)', () => {
    abrir();
    const nome = component.form.get('name')!;

    nome.setValue('N'.repeat(120));
    expect(nome.valid).toBeTrue();

    nome.setValue('N'.repeat(121));
    expect(nome.invalid).toBeTrue();
    expect(nome.hasError('maxlength')).toBeTrue();
  });

  it('CT-1.1.5 mantém phoneValidator e cepValidator no formulário, rejeitando entradas inválidas', () => {
    abrir();

    expect(phoneValidator()(new FormControl('113000001'))).not.toBeNull();
    expect(phoneValidator()(new FormControl('abc'))).not.toBeNull();
    expect(phoneValidator()(new FormControl('1130000001'))).toBeNull();
    expect(cepValidator()(new FormControl('1234'))).not.toBeNull();
    expect(cepValidator()(new FormControl('13010-000'))).toBeNull();

    component.form.get('phone')!.setValue('113000001');
    component.form.get('zipCode')!.setValue('1234');
    expect(component.form.get('phone')!.hasError('phone')).toBeTrue();
    expect(component.form.get('zipCode')!.hasError('cep')).toBeTrue();
  });

  it('CT-1.1.9 desabilita Salvar quando o formulário é inválido e habilita quando é válido e sem estados ativos', () => {
    abrir();
    const salvar = () => host().querySelector('button[mat-raised-button]') as HTMLButtonElement;

    fixture.detectChanges();
    expect(component.form.invalid).toBeTrue();
    expect(salvar().disabled).toBeTrue();

    component.form.patchValue({ name: 'Fornecedor Teste N' });
    fixture.detectChanges();
    expect(salvar().disabled).toBeFalse();
  });

  it('CT-1.1.9 desabilita Salvar enquanto loading, loadFailed ou saving estiverem ativos, mesmo com formulário válido', () => {
    abrir();
    component.form.patchValue({ name: 'Fornecedor Teste N' });
    const salvar = () => host().querySelector('button[mat-raised-button]') as HTMLButtonElement;

    (['loading', 'loadFailed', 'saving'] as const).forEach(estado => {
      component[estado] = true;
      fixture.detectChanges();
      expect(salvar().disabled).withContext(estado).toBeTrue();

      component[estado] = false;
      fixture.detectChanges();
      expect(salvar().disabled).withContext(`${estado} desligado`).toBeFalse();
    });
  });

  it('CT-1.1.9 mantém o botão Cancelar com mat-dialog-close', () => {
    abrir();

    const botoes = Array.from(host().querySelectorAll('button')) as HTMLButtonElement[];
    const cancelar = botoes.find(b => (b.textContent ?? '').includes('Cancelar'));
    expect(cancelar).toBeDefined();
    expect(cancelar!.hasAttribute('mat-dialog-close')).toBeTrue();
  });

  it('CT-1.1.10 mostra no mat-error do telefone a PHONE_ERROR_MESSAGE após um 400 de telefone', () => {
    service.createSupplier.and.returnValue(throwError(() => ({
      status: 400,
      error: { erros: ['phone: formato inválido'] }
    })));
    abrir();
    component.form.patchValue({ name: 'Fornecedor Teste N', phone: '(11) 3000-0001' });

    component.onSubmit();
    fixture.detectChanges();

    expect(mensagensDeErro('phone')).toEqual([PHONE_ERROR_MESSAGE]);
  });

  it('CT-1.1.11 mostra o erro 400 de zipCode e street nos campos, mantém o diálogo aberto e preserva os valores digitados', () => {
    service.createSupplier.and.returnValue(throwError(() => ({
      status: 400,
      error: { erros: ['zipCode: CEP não encontrado', 'street: logradouro inválido'] }
    })));
    abrir();
    component.form.patchValue({
      name: 'Fornecedor Teste N',
      zipCode: '13010-000',
      street: 'Rua das Flores',
      number: '120'
    });

    component.onSubmit();
    fixture.detectChanges();

    expect(mensagensDeErro('zipCode')).toEqual(['CEP não encontrado']);
    expect(mensagensDeErro('street')).toEqual(['logradouro inválido']);
    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component.form.get('name')!.value).toBe('Fornecedor Teste N');
    expect(component.form.get('zipCode')!.value).toBe('13010-000');
    expect(component.form.get('street')!.value).toBe('Rua das Flores');
    expect(component.form.get('number')!.value).toBe('120');
  });

  it('CT-1.1.12 preenche cada campo do formulário na edição com o endereço completo devolvido por getSupplierById', () => {
    service.getSupplierById.and.returnValue(of({ data: FORNECEDOR_COMPLETO } as any));

    abrir({ id: FORNECEDOR_COMPLETO.id });

    const v = component.form.value;
    expect(service.getSupplierById).toHaveBeenCalledWith(FORNECEDOR_COMPLETO.id);
    expect(String(v.zipCode).replace(/\D/g, '')).toBe('13010000');
    expect(v.street).toBe('Rua das Flores');
    expect(v.number).toBe('120');
    expect(v.complement).toBe('sala 3');
    expect(v.neighborhood).toBe('Centro');
    expect(v.city).toBe('Campinas');
    expect(v.state).toBe('SP');
    expect(v.country).toBe('BR');
    expect(v.reference).toBe('ref-01');
    expect((campo('street') as HTMLInputElement).value).toBe('Rua das Flores');
  });
});
