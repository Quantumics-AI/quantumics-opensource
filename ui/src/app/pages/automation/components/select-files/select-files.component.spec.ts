import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SelectFilesComponent } from './select-files.component';

describe('SelectFilesComponent', () => {
  let component: SelectFilesComponent;
  let fixture: ComponentFixture<SelectFilesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectFilesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectFilesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
