import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FrequencyAnalysisComponent } from './frequency-analysis.component';

describe('FrequencyAnalysisComponent', () => {
  let component: FrequencyAnalysisComponent;
  let fixture: ComponentFixture<FrequencyAnalysisComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FrequencyAnalysisComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FrequencyAnalysisComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
