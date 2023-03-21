import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DeltaComponent } from './delta.component';

describe('DeltaComponent', () => {
  let component: DeltaComponent;
  let fixture: ComponentFixture<DeltaComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ DeltaComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeltaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
