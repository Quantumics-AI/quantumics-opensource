import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PiiComponent } from './pii.component';

describe('PiiComponent', () => {
  let component: PiiComponent;
  let fixture: ComponentFixture<PiiComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PiiComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PiiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
