import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EditFolderComponent } from './edit-folder.component';

describe('EditFolderComponent', () => {
  let component: EditFolderComponent;
  let fixture: ComponentFixture<EditFolderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ EditFolderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditFolderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
