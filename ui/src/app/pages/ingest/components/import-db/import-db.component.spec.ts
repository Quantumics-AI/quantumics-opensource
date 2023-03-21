import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ImportDbComponent } from './import-db.component';

describe('ImportDbComponent', () => {
  let component: ImportDbComponent;
  let fixture: ComponentFixture<ImportDbComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ImportDbComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImportDbComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
