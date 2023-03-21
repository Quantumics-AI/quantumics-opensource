import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { StandardizeComponent } from './standardize.component';

describe('StandardizeComponent', () => {
  let component: StandardizeComponent;
  let fixture: ComponentFixture<StandardizeComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ StandardizeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StandardizeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
