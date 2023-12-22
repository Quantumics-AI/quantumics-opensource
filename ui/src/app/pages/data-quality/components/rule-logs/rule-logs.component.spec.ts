import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RuleLogsComponent } from './rule-logs.component';

describe('RuleLogsComponent', () => {
  let component: RuleLogsComponent;
  let fixture: ComponentFixture<RuleLogsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RuleLogsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RuleLogsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
