import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AddCleansingComponent } from './add-cleansing.component';

describe('AddCleansingComponent', () => {
  let component: AddCleansingComponent;
  let fixture: ComponentFixture<AddCleansingComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AddCleansingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddCleansingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
