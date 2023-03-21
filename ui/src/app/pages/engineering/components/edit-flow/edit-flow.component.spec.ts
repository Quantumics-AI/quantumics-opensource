import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditFlowComponent } from './edit-flow.component';

describe('EditFlowComponent', () => {
  let component: EditFlowComponent;
  let fixture: ComponentFixture<EditFlowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditFlowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditFlowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
