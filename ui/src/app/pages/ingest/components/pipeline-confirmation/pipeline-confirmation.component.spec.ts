import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PipelineConfirmationComponent } from './pipeline-confirmation.component';

describe('PipelineConfirmationComponent', () => {
  let component: PipelineConfirmationComponent;
  let fixture: ComponentFixture<PipelineConfirmationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PipelineConfirmationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PipelineConfirmationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
