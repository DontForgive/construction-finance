import { Payer } from "../payer/Payer";
import { Supplier } from "../supplier/supplier";

export interface Expense {
  id: number;
  date: string;           
  description: string;
  supplierId: number;
  payerId: number;
  paymentMethod: string;
  amount: number;        
  attachmentUrl: string | null;
  supplier: Supplier;
  payer: Payer;
}