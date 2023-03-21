import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EngineeringUdfComponent } from './engineering-udf.component';

describe('EngineeringUdfComponent', () => {
  let component: EngineeringUdfComponent;
  let fixture: ComponentFixture<EngineeringUdfComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ EngineeringUdfComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EngineeringUdfComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
