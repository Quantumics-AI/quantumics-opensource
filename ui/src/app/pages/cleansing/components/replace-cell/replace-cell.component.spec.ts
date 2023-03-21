import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ReplaceCellComponent } from './replace-cell.component';

describe('ReplaceCellComponent', () => {
  let component: ReplaceCellComponent;
  let fixture: ComponentFixture<ReplaceCellComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ReplaceCellComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReplaceCellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
