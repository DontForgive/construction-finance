import { Component, Inject } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ApiResponse } from 'app/utils/response';
import { SupplierService } from './supplier.service';
import { Supplier, SupplierPayload } from './supplier';
import Swal from 'sweetalert2';
import { ToastService } from 'app/utils/toastr';
import {
  ADDRESS_FIELDS,
  ADDRESS_MAX_LENGTH,
  CEP_DIGITS,
  COUNTRY_OPTIONS,
  SelectOption,
  UF_OPTIONS,
  cepValidator,
  digitsOnly,
  formatCep,
  maxTrimmedLength,
  notBlank,
  parseServerErrors,
  textOrNull
} from './supplier-address';

@Component({
  selector: 'app-supplier-add-dialog',
  template: `
    <h2 mat-dialog-title class="mat-headline-6 d-flex align-items-center">
        <mat-icon color="primary" class="me-2">category</mat-icon>
        <span class="text-muted">{{ title }}</span>
    </h2>

    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <mat-dialog-content>
        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>Nome</mat-label>
          <input matInput formControlName="name" maxlength="120" required>
          <mat-error *ngIf="form.get('name')?.hasError('required')">
            Nome é obrigatório
          </mat-error>
          <mat-error *ngIf="form.get('name')?.hasError('maxlength')">
            Nome deve ter no máximo 120 caracteres
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>Chave PIX</mat-label>
          <input matInput formControlName="keyPix">
        </mat-form-field>

        <mat-checkbox formControlName="worker" class="d-block mb-3">
          É prestador de Serviço?
        </mat-checkbox>

        <h3 class="mat-subheading-2 mb-2">Endereço (opcional)</h3>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>CEP</mat-label>
          <input matInput formControlName="zipCode" inputmode="numeric" maxlength="9"
                 placeholder="00000-000" (input)="onCepInput()">
          <mat-error *ngIf="fieldError('zipCode') as msg">{{ msg }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>Rua</mat-label>
          <input matInput formControlName="street">
          <mat-error *ngIf="fieldError('street') as msg">{{ msg }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>Número</mat-label>
          <input matInput formControlName="number">
          <mat-error *ngIf="fieldError('number') as msg">{{ msg }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>Complemento</mat-label>
          <input matInput formControlName="complement">
          <mat-error *ngIf="fieldError('complement') as msg">{{ msg }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>Bairro</mat-label>
          <input matInput formControlName="neighborhood">
          <mat-error *ngIf="fieldError('neighborhood') as msg">{{ msg }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>Cidade</mat-label>
          <input matInput formControlName="city">
          <mat-error *ngIf="fieldError('city') as msg">{{ msg }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>UF</mat-label>
          <mat-select formControlName="state">
            <mat-option value="">Não informado</mat-option>
            <mat-option *ngFor="let uf of ufOptions" [value]="uf.code">{{ uf.code }} - {{ uf.name }}</mat-option>
          </mat-select>
          <mat-error *ngIf="fieldError('state') as msg">{{ msg }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>País</mat-label>
          <mat-select formControlName="country">
            <mat-option value="">Não informado</mat-option>
            <mat-option *ngFor="let c of countryOptions" [value]="c.code">{{ c.code }} - {{ c.name }}</mat-option>
          </mat-select>
          <mat-error *ngIf="fieldError('country') as msg">{{ msg }}</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-100 mb-3">
          <mat-label>Referência</mat-label>
          <input matInput formControlName="reference">
          <mat-error *ngIf="fieldError('reference') as msg">{{ msg }}</mat-error>
        </mat-form-field>

      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-stroked-button color="warn" mat-dialog-close type="button">
          <mat-icon class="me-1">close</mat-icon>
          Cancelar
        </button>
        <button mat-raised-button color="primary" type="submit"
                [disabled]="form.invalid || loading || loadFailed || saving">
          <mat-icon class="me-1">save</mat-icon>
          Salvar
        </button>
      </mat-dialog-actions>
    </form>
  `
})
export class SupplierAddDialogComponent {
  form: FormGroup;

  readonly ufOptions: SelectOption[] = UF_OPTIONS;
  readonly countryOptions: SelectOption[] = COUNTRY_OPTIONS;

  /** Carregando o detalhe do fornecedor (edição). */
  loading = false;
  /** Falha ao carregar o detalhe: salvar fica bloqueado para não limpar o endereço. */
  loadFailed = false;
  /** Envio em andamento. */
  saving = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<SupplierAddDialogComponent>,
    private supplierService: SupplierService,
    private toast: ToastService,
    @Inject(MAT_DIALOG_DATA) public data: any

  ) {
    this.form = this.fb.group({
      name: [data?.name || '', [Validators.required, notBlank(), Validators.maxLength(120)]],
      keyPix: [data?.keyPix || ''],
      worker: [data?.worker || false],
      zipCode: ['', [cepValidator()]],
      street: ['', [maxTrimmedLength(ADDRESS_MAX_LENGTH.street)]],
      number: ['', [maxTrimmedLength(ADDRESS_MAX_LENGTH.number)]],
      complement: ['', [maxTrimmedLength(ADDRESS_MAX_LENGTH.complement)]],
      neighborhood: ['', [maxTrimmedLength(ADDRESS_MAX_LENGTH.neighborhood)]],
      city: ['', [maxTrimmedLength(ADDRESS_MAX_LENGTH.city)]],
      state: [''],
      country: [''],
      reference: ['', [maxTrimmedLength(ADDRESS_MAX_LENGTH.reference)]]
    });

    if (this.data?.id) {
      this.loadSupplier(this.data.id);
    } else {
      this.fillAddress(this.data);
    }
  }

  get title(): string {
    return this.data?.id ? 'Editar Fornecedor' : 'Adicionar Fornecedor';
  }

  /** Aplica a máscara visual do CEP enquanto o usuário digita. */
  onCepInput() {
    const control = this.form.get('zipCode');
    if (!control) {
      return;
    }
    const masked = formatCep(control.value);
    if (masked !== control.value) {
      control.setValue(masked);
    }
  }

  /** Mensagem de erro do campo (validação local ou devolvida pela API). */
  fieldError(field: string): string | null {
    const errors = this.form.get(field)?.errors;
    if (!errors) {
      return null;
    }
    if (errors['server']) {
      return errors['server'];
    }
    if (errors['cep']) {
      return `CEP deve conter ${CEP_DIGITS} dígitos`;
    }
    if (errors['maxlength']) {
      const max = errors['maxlength'].requiredLength;
      return `Deve ter no máximo ${max} caracteres`;
    }
    return null;
  }

  private loadSupplier(id: number) {
    this.loading = true;
    this.supplierService.getSupplierById(id).subscribe({
      next: (res) => {
        const supplier = res?.data as Supplier;
        this.form.patchValue({
          name: supplier?.name ?? this.form.get('name')?.value,
          keyPix: supplier?.keyPix ?? '',
          worker: supplier?.worker ?? false
        });
        this.fillAddress(supplier);
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.loadFailed = true;
        Swal.fire({
          icon: 'error',
          title: 'Erro ao carregar o Fornecedor',
          text: err?.error?.message || 'Erro desconhecido',
        });
      }
    });
  }

  private fillAddress(source: Partial<Supplier> | null | undefined) {
    this.form.patchValue({
      zipCode: formatCep(source?.zipCode),
      street: source?.street ?? '',
      number: source?.number ?? '',
      complement: source?.complement ?? '',
      neighborhood: source?.neighborhood ?? '',
      city: source?.city ?? '',
      state: source?.state ?? '',
      country: source?.country ?? '',
      reference: source?.reference ?? ''
    });
  }

  private buildPayload(): SupplierPayload {
    const v = this.form.value;
    const zipDigits = digitsOnly(v.zipCode);

    return {
      name: (v.name ?? '').toString().trim(),
      keyPix: textOrNull(v.keyPix) as any,
      worker: !!v.worker,
      zipCode: zipDigits === '' ? null : zipDigits,
      street: textOrNull(v.street),
      number: textOrNull(v.number),
      complement: textOrNull(v.complement),
      neighborhood: textOrNull(v.neighborhood),
      city: textOrNull(v.city),
      state: textOrNull(v.state),
      country: textOrNull(v.country),
      reference: textOrNull(v.reference)
    };
  }

  /**
   * Erros 400 de validação: mostra o detalhe no campo correspondente e mantém o diálogo aberto.
   * Demais erros (409, 500, ...) mantêm a mensagem geral, sem expor o texto técnico de `erros`.
   */
  private handleError(err: any, title: string) {
    this.saving = false;

    let text = err?.error?.message || 'Erro desconhecido';

    if (err?.status === 400) {
      const parsed = parseServerErrors(err?.error?.erros, ADDRESS_FIELDS);
      const fieldNames = Object.keys(parsed.fields);

      fieldNames.forEach(field => {
        const control: AbstractControl | null = this.form.get(field);
        control?.setErrors({ server: parsed.fields[field] });
        control?.markAsTouched();
      });

      if (parsed.general.length > 0) {
        text = parsed.general.join('\n');
      } else if (fieldNames.length > 0) {
        this.toast.error('Corrija os campos destacados.');
        return;
      }
    }

    Swal.fire({ icon: 'error', title, text });
  }

  onSubmit() {
    if (this.form.invalid || this.loading || this.loadFailed || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    const supplier = this.buildPayload();
    this.saving = true;

    if (this.data?.id) {
      this.supplierService.updateSupplier(this.data.id, supplier).subscribe({
        next: (res: any) => {
          this.toast.success(
            'Fornecedor atualizado com sucesso!',
          );
          this.dialogRef.close(res.data);
        },
        error: (err) => this.handleError(err, 'Erro ao atualizar o Fornecedor')
      });
    } else {
      this.supplierService.createSupplier(supplier).subscribe({
        next: (res: ApiResponse<Supplier>) => {

          this.toast.success(
            'Fornecedor criado com sucesso!',
          );


          this.dialogRef.close(res.data);
        },
        error: (err) => this.handleError(err, 'Erro ao criar Fornecedor')
      });
    }

  }
}
