import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CreateUdfComponent } from './create-udf.component';

describe('CreateUdfComponent', () => {
  let component: CreateUdfComponent;
  let fixture: ComponentFixture<CreateUdfComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateUdfComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUdfComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
