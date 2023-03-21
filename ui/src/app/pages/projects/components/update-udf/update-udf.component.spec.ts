import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UpdateUdfComponent } from './update-udf.component';

describe('UpdateUdfComponent', () => {
  let component: UpdateUdfComponent;
  let fixture: ComponentFixture<UpdateUdfComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UpdateUdfComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateUdfComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
