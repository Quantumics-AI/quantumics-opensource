import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UpdatePipelineComponent } from './update-pipeline.component';

describe('UpdatePipelineComponent', () => {
  let component: UpdatePipelineComponent;
  let fixture: ComponentFixture<UpdatePipelineComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UpdatePipelineComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdatePipelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
