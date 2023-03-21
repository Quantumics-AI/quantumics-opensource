import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PreviewJoinComponent } from './preview-join.component';

describe('PreviewJoinComponent', () => {
  let component: PreviewJoinComponent;
  let fixture: ComponentFixture<PreviewJoinComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PreviewJoinComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PreviewJoinComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
