import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DictionaryRuleComponent } from './dictionary-rule.component';

describe('DictionaryRuleComponent', () => {
  let component: DictionaryRuleComponent;
  let fixture: ComponentFixture<DictionaryRuleComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ DictionaryRuleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DictionaryRuleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
