import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewRunningRulesComponent } from './view-running-rules.component';

describe('ViewRunningRulesComponent', () => {
  let component: ViewRunningRulesComponent;
  let fixture: ComponentFixture<ViewRunningRulesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewRunningRulesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewRunningRulesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
