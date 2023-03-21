import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConnectSuccessComponent } from './connect-success.component';

describe('ConnectSuccessComponent', () => {
  let component: ConnectSuccessComponent;
  let fixture: ComponentFixture<ConnectSuccessComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ConnectSuccessComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectSuccessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
