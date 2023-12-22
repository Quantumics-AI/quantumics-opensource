import { TestBed } from '@angular/core/testing';

import { RuleCreationService } from './rule-creation.service';

describe('RuleCreationService', () => {
  let service: RuleCreationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RuleCreationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
