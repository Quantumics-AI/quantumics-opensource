import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewResultComponent } from './view-result.component';

describe('ViewResultComponent', () => {
  let component: ViewResultComponent;
  let fixture: ComponentFixture<ViewResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewResultComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
