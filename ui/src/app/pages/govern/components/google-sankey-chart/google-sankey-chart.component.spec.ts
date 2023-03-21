import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { GoogleSankeyChartComponent } from './google-sankey-chart.component';

describe('GoogleSankeyChartComponent', () => {
  let component: GoogleSankeyChartComponent;
  let fixture: ComponentFixture<GoogleSankeyChartComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ GoogleSankeyChartComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GoogleSankeyChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
