import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConnectToApiComponent } from './connect-to-api.component';

describe('ConnectToApiComponent', () => {
  let component: ConnectToApiComponent;
  let fixture: ComponentFixture<ConnectToApiComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ConnectToApiComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectToApiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
