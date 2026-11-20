export interface Supplier {
  id: number;
  name: string;
  keyPix: string;
  worker: boolean;
  phone?: string | null;
  zipCode?: string | null;
  street?: string | null;
  number?: string | null;
  complement?: string | null;
  neighborhood?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  reference?: string | null;
}

/** Corpo enviado em POST/PUT de fornecedor (estado completo do formulário). */
export interface SupplierPayload {
  name: string;
  keyPix: string;
  worker: boolean;
  /** Somente dígitos ou null. Omitido na edição significa "não alterado". */
  phone?: string | null;
  zipCode: string | null;
  street: string | null;
  number: string | null;
  complement: string | null;
  neighborhood: string | null;
  city: string | null;
  state: string | null;
  country: string | null;
  reference: string | null;
}
