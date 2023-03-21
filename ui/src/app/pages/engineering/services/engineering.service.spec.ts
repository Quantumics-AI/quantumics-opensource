import { TestBed } from '@angular/core/testing';

import { EngineeringService } from './engineering.service';

describe('EngineeringService', () => {
  let service: EngineeringService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EngineeringService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
