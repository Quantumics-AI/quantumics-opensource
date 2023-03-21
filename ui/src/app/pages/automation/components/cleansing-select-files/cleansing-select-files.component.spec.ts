import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CleansingSelectFilesComponent } from './cleansing-select-files.component';

describe('CleansingSelectFilesComponent', () => {
  let component: CleansingSelectFilesComponent;
  let fixture: ComponentFixture<CleansingSelectFilesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CleansingSelectFilesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CleansingSelectFilesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
