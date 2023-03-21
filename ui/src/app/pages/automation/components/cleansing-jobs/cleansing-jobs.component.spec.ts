import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CleansingJobsComponent } from './cleansing-jobs.component';

describe('CleansingJobsComponent', () => {
  let component: CleansingJobsComponent;
  let fixture: ComponentFixture<CleansingJobsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CleansingJobsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CleansingJobsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
