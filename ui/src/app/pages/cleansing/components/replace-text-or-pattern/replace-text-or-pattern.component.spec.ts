import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ReplaceTextOrPatternComponent } from './replace-text-or-pattern.component';

describe('ReplaceTextOrPatternComponent', () => {
  let component: ReplaceTextOrPatternComponent;
  let fixture: ComponentFixture<ReplaceTextOrPatternComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ReplaceTextOrPatternComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReplaceTextOrPatternComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
