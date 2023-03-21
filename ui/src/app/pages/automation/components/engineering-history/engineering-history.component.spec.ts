import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EngineeringHistoryComponent } from './engineering-history.component';

describe('EngineeringHistoryComponent', () => {
  let component: EngineeringHistoryComponent;
  let fixture: ComponentFixture<EngineeringHistoryComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ EngineeringHistoryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EngineeringHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
