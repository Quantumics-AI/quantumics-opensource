import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SankeyChartComponent } from './sankey-chart.component';

describe('SankeyChartComponent', () => {
  let component: SankeyChartComponent;
  let fixture: ComponentFixture<SankeyChartComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SankeyChartComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SankeyChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
