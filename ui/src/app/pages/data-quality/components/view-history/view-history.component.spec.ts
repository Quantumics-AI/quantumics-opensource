import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewHistoryComponent } from './view-history.component';

describe('ViewHistoryComponent', () => {
  let component: ViewHistoryComponent;
  let fixture: ComponentFixture<ViewHistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewHistoryComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
