import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ListSourceDataComponent } from './list-source-data.component';

describe('ListSourceDataComponent', () => {
  let component: ListSourceDataComponent;
  let fixture: ComponentFixture<ListSourceDataComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ListSourceDataComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListSourceDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
