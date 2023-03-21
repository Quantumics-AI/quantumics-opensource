import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConnectDbComponent } from './connect-db.component';

describe('ConnectDbComponent', () => {
  let component: ConnectDbComponent;
  let fixture: ComponentFixture<ConnectDbComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ConnectDbComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectDbComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
