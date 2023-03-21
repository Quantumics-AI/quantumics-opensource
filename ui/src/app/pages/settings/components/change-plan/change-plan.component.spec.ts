import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ChangePlanComponent } from './change-plan.component';

describe('ChangePlanComponent', () => {
  let component: ChangePlanComponent;
  let fixture: ComponentFixture<ChangePlanComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ChangePlanComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChangePlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
