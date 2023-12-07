import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ColumnAnalysisComponent } from './column-analysis.component';

describe('ColumnAnalysisComponent', () => {
  let component: ColumnAnalysisComponent;
  let fixture: ComponentFixture<ColumnAnalysisComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ColumnAnalysisComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ColumnAnalysisComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
