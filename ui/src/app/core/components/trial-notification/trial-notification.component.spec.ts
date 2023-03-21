import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TrialNotificationComponent } from './trial-notification.component';

describe('TrialNotificationComponent', () => {
  let component: TrialNotificationComponent;
  let fixture: ComponentFixture<TrialNotificationComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TrialNotificationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TrialNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
