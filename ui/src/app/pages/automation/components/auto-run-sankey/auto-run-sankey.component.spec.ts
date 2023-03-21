import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AutoRunSankeyComponent } from './auto-run-sankey.component';

describe('AutoRunSankeyComponent', () => {
  let component: AutoRunSankeyComponent;
  let fixture: ComponentFixture<AutoRunSankeyComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AutoRunSankeyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AutoRunSankeyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
