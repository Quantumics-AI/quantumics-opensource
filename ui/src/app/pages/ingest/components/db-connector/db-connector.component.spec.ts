import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DbConnectorComponent } from './db-connector.component';

describe('DbConnectorComponent', () => {
  let component: DbConnectorComponent;
  let fixture: ComponentFixture<DbConnectorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ DbConnectorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DbConnectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
