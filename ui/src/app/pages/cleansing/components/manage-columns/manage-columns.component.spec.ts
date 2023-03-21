import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ManageColumnsComponent } from './manage-columns.component';

describe('ManageColumnsComponent', () => {
  let component: ManageColumnsComponent;
  let fixture: ComponentFixture<ManageColumnsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ManageColumnsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManageColumnsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
