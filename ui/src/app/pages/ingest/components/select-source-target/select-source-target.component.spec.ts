import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectSourceTargetComponent } from './select-source-target.component';

describe('SelectSourceTargetComponent', () => {
  let component: SelectSourceTargetComponent;
  let fixture: ComponentFixture<SelectSourceTargetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectSourceTargetComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SelectSourceTargetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
