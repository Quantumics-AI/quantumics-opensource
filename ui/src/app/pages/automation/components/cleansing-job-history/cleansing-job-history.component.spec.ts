import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CleansingJobHistoryComponent } from './cleansing-job-history.component';

describe('CleansingJobHistoryComponent', () => {
  let component: CleansingJobHistoryComponent;
  let fixture: ComponentFixture<CleansingJobHistoryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CleansingJobHistoryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CleansingJobHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
