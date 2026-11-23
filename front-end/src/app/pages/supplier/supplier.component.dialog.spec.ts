import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ToastService } from 'app/utils/toastr';
import { SupplierAddDialogComponent } from './supplier-add-dialog.component';
import { SupplierComponent } from './supplier.component';
import { SupplierService } from './supplier.service';
import { Supplier } from './supplier';

const FORNECEDOR: Supplier = { id: 7, name: 'Fornecedor Teste Alfa', keyPix: 'chave-ficticia-01', worker: true };

const OPCOES_DE_TAMANHO = {
  width: '720px',
  maxWidth: 'calc(100vw - 32px)',
  maxHeight: 'calc(100vh - 32px)'
};

describe('SupplierComponent - abertura do diálogo (casos HU-1.1 UX)', () => {
  let component: SupplierComponent;
  let dialog: jasmine.SpyObj<MatDialog>;

  beforeEach(() => {
    const service = jasmine.createSpyObj<SupplierService>('SupplierService', ['getSuppliers', 'deleteSupplier']);
    const toast = jasmine.createSpyObj<ToastService>('ToastService', ['error', 'success']);
    dialog = jasmine.createSpyObj<MatDialog>('MatDialog', ['open']);
    dialog.open.and.returnValue({ afterClosed: () => of(false) } as any);
    component = new SupplierComponent(service, dialog, toast);
  });

  function opcoesUsadas(): any {
    expect(dialog.open).toHaveBeenCalledTimes(1);
    expect(dialog.open.calls.mostRecent().args[0]).toBe(SupplierAddDialogComponent);
    return dialog.open.calls.mostRecent().args[1];
  }

  it('CT-1.1.6 abre o diálogo de cadastro com width 720px, maxWidth e maxHeight calculados pela viewport', () => {
    component.openAddDialog();

    expect(opcoesUsadas()).toEqual(jasmine.objectContaining(OPCOES_DE_TAMANHO));
  });

  it('CT-1.1.7 abre o diálogo de edição com as mesmas opções de tamanho e com data.id do fornecedor', () => {
    component.openEditDialog(FORNECEDOR);

    const opcoes = opcoesUsadas();
    expect(opcoes).toEqual(jasmine.objectContaining(OPCOES_DE_TAMANHO));
    expect(opcoes.data.id).toBe(FORNECEDOR.id);
  });

  it('CT-1.1.8 não altera as demais opções do cadastro em relação ao baseline (somente as três opções de tamanho)', () => {
    component.openAddDialog();

    // Baseline anterior à mudança: { width: '500px' }, sem data e sem outras opções.
    expect(Object.keys(opcoesUsadas()).sort()).toEqual(['maxHeight', 'maxWidth', 'width']);
  });

  it('CT-1.1.8 não altera as demais opções da edição em relação ao baseline (tamanho + data com o próprio fornecedor)', () => {
    component.openEditDialog(FORNECEDOR);

    // Baseline anterior à mudança: { width: '500px', data: supplier }.
    const opcoes = opcoesUsadas();
    expect(Object.keys(opcoes).sort()).toEqual(['data', 'maxHeight', 'maxWidth', 'width']);
    expect(opcoes.data).toBe(FORNECEDOR);
  });
});
