import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PreviewFileComponent } from './preview-file.component';

describe('PreviewFileComponent', () => {
  let component: PreviewFileComponent;
  let fixture: ComponentFixture<PreviewFileComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PreviewFileComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PreviewFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
