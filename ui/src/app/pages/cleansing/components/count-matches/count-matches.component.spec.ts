import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CountMatchesComponent } from './count-matches.component';

describe('CountMatchesComponent', () => {
  let component: CountMatchesComponent;
  let fixture: ComponentFixture<CountMatchesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CountMatchesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CountMatchesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
