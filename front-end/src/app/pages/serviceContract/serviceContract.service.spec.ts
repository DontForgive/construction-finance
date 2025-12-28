/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ServiceContractService } from './serviceContract.service';

describe('Service: ServiceContract', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ServiceContractService]
    });
  });

  it('should ...', inject([ServiceContractService], (service: ServiceContractService) => {
    expect(service).toBeTruthy();
  }));
});
