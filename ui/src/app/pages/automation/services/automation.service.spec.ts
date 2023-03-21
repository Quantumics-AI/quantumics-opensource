import { TestBed } from '@angular/core/testing';

import { AutomationService } from './automation.service';

describe('AutomationService', () => {
  let service: AutomationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AutomationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
