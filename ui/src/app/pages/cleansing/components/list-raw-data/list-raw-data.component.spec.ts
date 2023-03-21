import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ListRawDataComponent } from './list-raw-data.component';

describe('ListRawDataComponent', () => {
  let component: ListRawDataComponent;
  let fixture: ComponentFixture<ListRawDataComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ListRawDataComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListRawDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
