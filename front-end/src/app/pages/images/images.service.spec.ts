/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ImagesService } from './images.service';

describe('Service: Images', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ImagesService]
    });
  });

  it('should ...', inject([ImagesService], (service: ImagesService) => {
    expect(service).toBeTruthy();
  }));
});
