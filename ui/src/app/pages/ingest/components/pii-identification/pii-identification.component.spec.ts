import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PiiIdentificationComponent } from './pii-identification.component';

describe('PiiIdentificationComponent', () => {
  let component: PiiIdentificationComponent;
  let fixture: ComponentFixture<PiiIdentificationComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PiiIdentificationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PiiIdentificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
