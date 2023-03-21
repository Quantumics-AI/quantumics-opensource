import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateUpdateUdfComponent } from './create-update-udf.component';

describe('CreateUpdateUdfComponent', () => {
  let component: CreateUpdateUdfComponent;
  let fixture: ComponentFixture<CreateUpdateUdfComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateUpdateUdfComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUpdateUdfComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
