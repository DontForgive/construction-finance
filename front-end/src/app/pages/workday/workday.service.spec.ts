/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { WorkdayService } from './workday.service';

describe('Service: Workday', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WorkdayService]
    });
  });

  it('should ...', inject([WorkdayService], (service: WorkdayService) => {
    expect(service).toBeTruthy();
  }));
});
