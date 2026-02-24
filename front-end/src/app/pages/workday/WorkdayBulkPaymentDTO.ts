export interface WorkdayBulkPaymentDTO {
  workdayIds: number[];
  supplierId: number;
  description: string;
  paymentDate: string;
  payerId: number;
  categoryId: number;
  serviceContractId: number;
  paymentMethod: string;
  amount: number;
}
