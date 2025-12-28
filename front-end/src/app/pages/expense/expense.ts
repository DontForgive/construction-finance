import { Payer } from "../payer/Payer";
import { ServiceContractDTO } from "../serviceContract/service-contract.dto";
import { Supplier } from "../supplier/supplier";

export interface Expense {
  id: number;
  date: string;           
  description: string;
  supplierId: number;
  payerId: number;
  serviceContractId: number;
  paymentMethod: string;
  amount: number;        
  attachmentUrl: string | null;
  supplier: Supplier;
  payer: Payer;
  serviceContract: ServiceContractDTO;
}