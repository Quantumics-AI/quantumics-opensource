import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PreviewAggregateComponent } from './preview-aggregate.component';

describe('PreviewAggregateComponent', () => {
  let component: PreviewAggregateComponent;
  let fixture: ComponentFixture<PreviewAggregateComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PreviewAggregateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PreviewAggregateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
