import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ImportLocalFileComponent } from './import-local-file.component';

describe('ImportLocalFileComponent', () => {
  let component: ImportLocalFileComponent;
  let fixture: ComponentFixture<ImportLocalFileComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ImportLocalFileComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImportLocalFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
