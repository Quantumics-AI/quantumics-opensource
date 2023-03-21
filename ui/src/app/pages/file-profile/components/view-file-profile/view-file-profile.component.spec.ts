import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ViewFileProfileComponent } from './view-file-profile.component';

describe('ViewFileProfileComponent', () => {
  let component: ViewFileProfileComponent;
  let fixture: ComponentFixture<ViewFileProfileComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewFileProfileComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewFileProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
