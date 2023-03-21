import { TestBed } from '@angular/core/testing';

import { GovernService } from './govern.service';

describe('GovernService', () => {
  let service: GovernService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GovernService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
