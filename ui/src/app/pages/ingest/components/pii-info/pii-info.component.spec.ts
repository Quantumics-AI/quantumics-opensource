import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PiiInfoComponent } from './pii-info.component';

describe('PiiInfoComponent', () => {
  let component: PiiInfoComponent;
  let fixture: ComponentFixture<PiiInfoComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PiiInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PiiInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
