import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ExtractColumnValuesComponent } from './extract-column-values.component';

describe('ExtractColumnValuesComponent', () => {
  let component: ExtractColumnValuesComponent;
  let fixture: ComponentFixture<ExtractColumnValuesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ExtractColumnValuesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExtractColumnValuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
