import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewIngestPipelineComponent } from './view-ingest-pipeline.component';

describe('ViewIngestPipelineComponent', () => {
  let component: ViewIngestPipelineComponent;
  let fixture: ComponentFixture<ViewIngestPipelineComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewIngestPipelineComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewIngestPipelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
