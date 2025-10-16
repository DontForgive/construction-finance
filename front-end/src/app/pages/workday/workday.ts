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