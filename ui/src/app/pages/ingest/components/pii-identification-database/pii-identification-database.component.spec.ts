import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PiiIdentificationDatabaseComponent } from './pii-identification-database.component';

describe('PiiIdentificationDatabaseComponent', () => {
  let component: PiiIdentificationDatabaseComponent;
  let fixture: ComponentFixture<PiiIdentificationDatabaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PiiIdentificationDatabaseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PiiIdentificationDatabaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
