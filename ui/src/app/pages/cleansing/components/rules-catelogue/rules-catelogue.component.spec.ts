import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RulesCatelogueComponent } from './rules-catelogue.component';

describe('RulesCatelogueComponent', () => {
  let component: RulesCatelogueComponent;
  let fixture: ComponentFixture<RulesCatelogueComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RulesCatelogueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RulesCatelogueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
