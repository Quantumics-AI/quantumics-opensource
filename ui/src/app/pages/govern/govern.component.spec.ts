import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { GovernComponent } from './govern.component';

describe('GovernComponent', () => {
  let component: GovernComponent;
  let fixture: ComponentFixture<GovernComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ GovernComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GovernComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
