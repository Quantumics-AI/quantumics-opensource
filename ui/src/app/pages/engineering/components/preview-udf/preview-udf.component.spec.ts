import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PreviewUdfComponent } from './preview-udf.component';

describe('PreviewUdfComponent', () => {
  let component: PreviewUdfComponent;
  let fixture: ComponentFixture<PreviewUdfComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PreviewUdfComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PreviewUdfComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
