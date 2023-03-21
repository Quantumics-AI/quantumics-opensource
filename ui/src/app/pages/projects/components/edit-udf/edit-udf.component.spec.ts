import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EditUdfComponent } from './edit-udf.component';

describe('EditUdfComponent', () => {
  let component: EditUdfComponent;
  let fixture: ComponentFixture<EditUdfComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ EditUdfComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditUdfComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
