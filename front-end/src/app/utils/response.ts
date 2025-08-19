export interface Page<T> {
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
