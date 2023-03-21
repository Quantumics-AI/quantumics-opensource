import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CleansingHistoryComponent } from './cleansing-history.component';

describe('CleansingHistoryComponent', () => {
  let component: CleansingHistoryComponent;
  let fixture: ComponentFixture<CleansingHistoryComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CleansingHistoryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CleansingHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
