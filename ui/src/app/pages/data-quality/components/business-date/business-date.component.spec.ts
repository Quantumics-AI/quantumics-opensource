import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BusinessDateComponent } from './business-date.component';

describe('BusinessDateComponent', () => {
  let component: BusinessDateComponent;
  let fixture: ComponentFixture<BusinessDateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BusinessDateComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BusinessDateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
