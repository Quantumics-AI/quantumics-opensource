import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CleansingToolbarComponent } from './cleansing-toolbar.component';

describe('CleansingToolbarComponent', () => {
  let component: CleansingToolbarComponent;
  let fixture: ComponentFixture<CleansingToolbarComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CleansingToolbarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CleansingToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
