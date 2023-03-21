import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SelectSourceTypeComponent } from './select-source-type.component';

describe('SelectSourceTypeComponent', () => {
  let component: SelectSourceTypeComponent;
  let fixture: ComponentFixture<SelectSourceTypeComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectSourceTypeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectSourceTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
