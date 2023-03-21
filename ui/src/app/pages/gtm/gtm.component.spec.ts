import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { GtmComponent } from './gtm.component';

describe('GtmComponent', () => {
  let component: GtmComponent;
  let fixture: ComponentFixture<GtmComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ GtmComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GtmComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
