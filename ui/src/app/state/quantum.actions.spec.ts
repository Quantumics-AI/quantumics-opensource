import * as QuantumActions from './quantum.actions';

describe('Quantum', () => {
  it('should create an instance', () => {
    expect(new QuantumActions.LoadQuantums()).toBeTruthy();
  });
});
