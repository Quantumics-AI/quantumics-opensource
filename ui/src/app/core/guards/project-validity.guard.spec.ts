import { TestBed } from '@angular/core/testing';

import { ProjectValidityGuard } from './project-validity.guard';

describe('ProjectValidityGuard', () => {
  let guard: ProjectValidityGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    guard = TestBed.inject(ProjectValidityGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
