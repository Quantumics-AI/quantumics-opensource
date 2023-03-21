import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CreateDbPipelineComponent } from './create-db-pipeline.component';

describe('CreateDbPipelineComponent', () => {
  let component: CreateDbPipelineComponent;
  let fixture: ComponentFixture<CreateDbPipelineComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateDbPipelineComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateDbPipelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
