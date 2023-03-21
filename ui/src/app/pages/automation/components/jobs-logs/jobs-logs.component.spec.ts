import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { JobsLogsComponent } from './jobs-logs.component';

describe('JobsLogsComponent', () => {
  let component: JobsLogsComponent;
  let fixture: ComponentFixture<JobsLogsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ JobsLogsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JobsLogsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
