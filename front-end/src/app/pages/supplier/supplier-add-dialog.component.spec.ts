import { FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';
import Swal from 'sweetalert2';
import { SupplierAddDialogComponent } from './supplier-add-dialog.component';
import { SupplierService } from './supplier.service';
import { PHONE_ERROR_MESSAGE } from './supplier-phone';

/** Fornecedor sintético com telefone fixo (CT-1.2.14, CT-1.2.20). */
const FORNECEDOR_A = { id: 1, name: 'Fornecedor Teste A', keyPix: '', worker: false, phone: '1130000001' };
/** Fornecedor sintético com telefone celular (CT-1.2.17). */
const FORNECEDOR_C = { id: 3, name: 'Fornecedor Teste C', keyPix: '', worker: false, phone: '11900000001' };

describe('SupplierAddDialogComponent - telefone (casos HU-1.2)', () => {
  let service: jasmine.SpyObj<SupplierService>;
  let dialogRef: jasmine.SpyObj<any>;
  let toast: jasmine.SpyObj<any>;

  function criarDialogo(data: any): SupplierAddDialogComponent {
    return new SupplierAddDialogComponent(new FormBuilder(), dialogRef, service, toast, data);
  }

  beforeEach(() => {
    service = jasmine.createSpyObj<SupplierService>('SupplierService',
      ['createSupplier', 'updateSupplier', 'getSupplierById']);
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    toast = jasmine.createSpyObj('ToastService', ['error', 'success']);
    spyOn(Swal, 'fire').and.returnValue(Promise.resolve({} as any));
  });

  it('CT-1.2.9 mostra a máscara ao digitar e envia somente dígitos na criação', () => {
    service.createSupplier.and.returnValue(of({ data: { id: 10 } } as any));
    const dialogo = criarDialogo({});
    dialogo.form.patchValue({ name: 'Fornecedor Teste N' });

    dialogo.form.get('phone')!.setValue('11900000001');
    dialogo.onPhoneInput();
    expect(dialogo.form.get('phone')!.value).toBe('(11) 90000-0001');

    dialogo.onSubmit();

    expect(service.createSupplier).toHaveBeenCalledTimes(1);
    expect(service.createSupplier.calls.mostRecent().args[0].phone).toBe('11900000001');
    expect(service.updateSupplier).not.toHaveBeenCalled();
  });

  it('CT-1.2.11 bloqueia telefone com 9 dígitos sem chamar a API e mostra a mensagem no campo', () => {
    const dialogo = criarDialogo({});
    dialogo.form.patchValue({ name: 'Fornecedor Teste N' });

    dialogo.form.get('phone')!.setValue('113000001');
    dialogo.onSubmit();

    expect(service.createSupplier).not.toHaveBeenCalled();
    expect(dialogo.fieldError('phone')).toBe(PHONE_ERROR_MESSAGE);
    expect(dialogo.form.get('phone')!.value).toBe('113000001');
  });

  it('CT-1.2.12 mostra o erro de telefone devolvido pela API no campo e mantém o diálogo aberto', () => {
    service.createSupplier.and.returnValue(throwError(() => ({
      status: 400,
      error: { erros: ['phone: formato inválido'] }
    })));
    const dialogo = criarDialogo({});
    dialogo.form.patchValue({ name: 'Fornecedor Teste N', keyPix: 'chave-01' });
    dialogo.form.get('phone')!.setValue('1130000001');

    dialogo.onSubmit();

    expect(dialogo.fieldError('phone')).toBe(PHONE_ERROR_MESSAGE);
    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(dialogo.form.get('name')!.value).toBe('Fornecedor Teste N');
    expect(dialogo.form.get('keyPix')!.value).toBe('chave-01');
  });

  it('CT-1.2.13 não mostra mensagem de telefone quando o 400 é de outro campo', () => {
    service.createSupplier.and.returnValue(throwError(() => ({
      status: 400,
      error: { erros: ['name: nome já utilizado'] }
    })));
    const dialogo = criarDialogo({});
    dialogo.form.patchValue({ name: 'Fornecedor Teste N' });
    dialogo.form.get('phone')!.setValue('1130000001');

    dialogo.onSubmit();

    expect(dialogo.fieldError('name')).toBe('nome já utilizado');
    expect(dialogo.fieldError('phone')).toBeNull();
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('CT-1.2.14 abre a edição com o telefone formatado e não envia nada antes de salvar', () => {
    service.getSupplierById.and.returnValue(of({ data: FORNECEDOR_A } as any));

    const dialogo = criarDialogo({ id: FORNECEDOR_A.id });

    expect(dialogo.form.get('phone')!.value).toBe('(11) 3000-0001');
    expect(service.updateSupplier).not.toHaveBeenCalled();
    expect(service.createSupplier).not.toHaveBeenCalled();
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('CT-1.2.16 envia o telefone alterado na edição e o valor novo fica no payload', () => {
    service.getSupplierById.and.returnValue(of({ data: FORNECEDOR_A } as any));
    service.updateSupplier.and.returnValue(of({ data: { id: 1 } } as any));
    const dialogo = criarDialogo({ id: FORNECEDOR_A.id, name: FORNECEDOR_A.name });

    dialogo.form.get('phone')!.setValue('(11) 90000-0001');
    dialogo.onSubmit();

    expect(service.updateSupplier).toHaveBeenCalledTimes(1);
    expect(service.updateSupplier.calls.mostRecent().args[1].phone).toBe('11900000001');
  });

  it('CT-1.2.17 apagar o telefone na edição envia telefone vazio ou nulo (L4)', () => {
    service.getSupplierById.and.returnValue(of({ data: FORNECEDOR_C } as any));
    service.updateSupplier.and.returnValue(of({ data: { id: 3 } } as any));
    const dialogo = criarDialogo({ id: FORNECEDOR_C.id, name: FORNECEDOR_C.name });

    dialogo.form.get('phone')!.setValue('');
    dialogo.onSubmit();

    expect(service.updateSupplier).toHaveBeenCalledTimes(1);
    const payload = service.updateSupplier.calls.mostRecent().args[1];
    // L4: o caso aceita null ou vazio para telefone apagado.
    expect([null, '']).toContain(payload.phone);
  });

  it('CT-1.2.20 bloqueia o salvamento quando o carregamento falha e não envia PUT', () => {
    service.getSupplierById.and.returnValue(throwError(() => ({ status: 500, error: { message: 'falha' } })));
    const dialogo = criarDialogo({ id: FORNECEDOR_A.id, name: FORNECEDOR_A.name });

    expect(dialogo.loadFailed).toBeTrue();
    dialogo.form.get('name')!.setValue('Outro nome');
    dialogo.onSubmit();

    expect(service.updateSupplier).not.toHaveBeenCalled();
  });

  it('CT-1.2.21 mantém os campos de endereço iguais aos enviados hoje na criação', () => {
    service.createSupplier.and.returnValue(of({ data: { id: 11 } } as any));
    const dialogo = criarDialogo({});
    dialogo.form.patchValue({
      name: 'Fornecedor Teste N',
      zipCode: '13010000',
      street: 'Rua das Flores',
      number: '120',
      city: 'Campinas',
      state: 'SP',
      country: 'BR'
    });
    dialogo.form.get('phone')!.setValue('1130000001');

    dialogo.onSubmit();

    const payload = service.createSupplier.calls.mostRecent().args[0] as any;
    expect(payload.zipCode).toBe('13010000');
    expect(payload.street).toBe('Rua das Flores');
    expect(payload.number).toBe('120');
    expect(payload.city).toBe('Campinas');
    expect(payload.state).toBe('SP');
    expect(payload.country).toBe('BR');
  });

  it('CT-1.2.22 não registra o telefone em console nem em mensagem quando o salvamento falha', () => {
    spyOn(console, 'log');
    spyOn(console, 'error');
    spyOn(console, 'warn');
    service.createSupplier.and.returnValue(throwError(() => ({ status: 500, error: {} })));
    const dialogo = criarDialogo({});
    dialogo.form.patchValue({ name: 'Fornecedor Teste N' });
    dialogo.form.get('phone')!.setValue('1130000001');

    dialogo.onSubmit();

    const registros = JSON.stringify([
      (console.log as jasmine.Spy).calls.allArgs(),
      (console.error as jasmine.Spy).calls.allArgs(),
      (console.warn as jasmine.Spy).calls.allArgs(),
      (Swal.fire as jasmine.Spy).calls.allArgs()
    ]);
    expect(registros).not.toContain('1130000001');
    expect(registros).not.toContain('(11) 3000-0001');
  });
});
