export interface Page<T> {
  id(id: any): unknown;
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ApiResponse<T> {
  status: number;
  message: string;
  data: Page<T>;
  erros: any[];
}

export interface ApiResponseTest<T>{
    status: number;
  message: string;
  data: T;
  erros: any[];
}

export interface ApiResponsCreate<T> {
  status: number;
  message: string;
  data: T;   // ✅ genérico, pode ser um Expense ou um Page<Expense>
  erros: any[];
}