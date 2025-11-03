export interface WorkDay {
  id?: number;
  date: string;
  supplierId: number;
  supplierName?: string;
  hoursWorked?: number;
  dailyValue?: number;
  note?: string;
  status?: string;
}

export interface WorkDayPaymentDTO {
  workdayIds: number[];
  description: string;
  paymentDate: string;  // formato ISO (yyyy-MM-dd)
  supplierId: number;
  categoryId?: number;  // opcional
}