/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { PayerService } from './payer.service';

describe('Service: Payer', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PayerService]
    });
  });

  it('should ...', inject([PayerService], (service: PayerService) => {
    expect(service).toBeTruthy();
  }));
});
