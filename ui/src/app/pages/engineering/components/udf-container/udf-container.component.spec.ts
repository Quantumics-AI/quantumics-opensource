import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UdfContainerComponent } from './udf-container.component';

describe('UdfContainerComponent', () => {
  let component: UdfContainerComponent;
  let fixture: ComponentFixture<UdfContainerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UdfContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UdfContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
