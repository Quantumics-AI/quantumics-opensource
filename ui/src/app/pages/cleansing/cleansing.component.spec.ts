import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CleansingComponent } from './cleansing.component';

describe('CleansingComponent', () => {
  let component: CleansingComponent;
  let fixture: ComponentFixture<CleansingComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CleansingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CleansingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
