import { Expense } from "../expense/expense";

export enum ContractStatus {
  ACTIVE = 'ACTIVE',
  CLOSED = 'CLOSED',
  CANCELED = 'CANCELED'
}


export interface ServiceContractDTO {
  id: number;
  name: string;
  description?: string;

  supplierId: number;
  supplierName: string;

  categoryId: number;
  categoryName: string;

  totalValue: number;
  paidValue: number;
  remainingValue: number;

  startDate: string; // ISO (yyyy-MM-dd)
  endDate?: string;

  status: ContractStatus;

  payments: Expense[];
}
