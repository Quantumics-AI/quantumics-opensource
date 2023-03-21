import { TestBed } from '@angular/core/testing';

import { UdfService } from './udf.service';

describe('UdfService', () => {
  let service: UdfService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UdfService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
