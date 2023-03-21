import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable } from 'rxjs';

import { QuantumEffects } from './quantum.effects';

describe('QuantumEffects', () => {
  let actions$: Observable<any>;
  let effects: QuantumEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        QuantumEffects,
        provideMockActions(() => actions$)
      ]
    });

    effects = TestBed.get<QuantumEffects>(QuantumEffects);
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });
});
