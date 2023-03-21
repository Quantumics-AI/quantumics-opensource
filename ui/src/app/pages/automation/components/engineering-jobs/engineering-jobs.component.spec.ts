import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EngineeringJobsComponent } from './engineering-jobs.component';

describe('EngineeringJobsComponent', () => {
  let component: EngineeringJobsComponent;
  let fixture: ComponentFixture<EngineeringJobsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ EngineeringJobsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EngineeringJobsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
