import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ParticleComponent } from './particle.component';

describe('ParticleComponent', () => {
  let component: ParticleComponent;
  let fixture: ComponentFixture<ParticleComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ParticleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ParticleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
