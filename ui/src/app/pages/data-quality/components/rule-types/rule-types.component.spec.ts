import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RuleTypesComponent } from './rule-types.component';

describe('RuleTypesComponent', () => {
  let component: RuleTypesComponent;
  let fixture: ComponentFixture<RuleTypesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RuleTypesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RuleTypesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
