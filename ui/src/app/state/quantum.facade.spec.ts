import { TestBed } from '@angular/core/testing';
import { Quantumfacade } from './quantum.facade';


describe('QuantumfacadeService', () => {
  let service: Quantumfacade;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Quantumfacade);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
