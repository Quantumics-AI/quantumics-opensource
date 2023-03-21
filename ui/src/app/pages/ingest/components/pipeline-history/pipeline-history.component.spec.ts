import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PipelineHistoryComponent } from './pipeline-history.component';

describe('PipelineHistoryComponent', () => {
  let component: PipelineHistoryComponent;
  let fixture: ComponentFixture<PipelineHistoryComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PipelineHistoryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PipelineHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
