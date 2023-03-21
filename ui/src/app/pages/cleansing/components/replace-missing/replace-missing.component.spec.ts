import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ReplaceMissingComponent } from './replace-missing.component';

describe('ReplaceMissingComponent', () => {
  let component: ReplaceMissingComponent;
  let fixture: ComponentFixture<ReplaceMissingComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ReplaceMissingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReplaceMissingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
