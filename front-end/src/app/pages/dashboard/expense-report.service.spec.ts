/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ExpenseReportService } from './expense-report.service';

describe('Service: ExpenseReport', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ExpenseReportService]
    });
  });

  it('should ...', inject([ExpenseReportService], (service: ExpenseReportService) => {
    expect(service).toBeTruthy();
  }));
});
