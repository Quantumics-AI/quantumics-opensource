import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UdfListComponent } from './udf-list.component';

describe('UdfListComponent', () => {
  let component: UdfListComponent;
  let fixture: ComponentFixture<UdfListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UdfListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UdfListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
