import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RowIdComponent } from './row-id.component';

describe('RowIdComponent', () => {
  let component: RowIdComponent;
  let fixture: ComponentFixture<RowIdComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RowIdComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RowIdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
