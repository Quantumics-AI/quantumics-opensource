import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SelectDbTableComponent } from './select-db-table.component';

describe('SelectDbTableComponent', () => {
  let component: SelectDbTableComponent;
  let fixture: ComponentFixture<SelectDbTableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectDbTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectDbTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
