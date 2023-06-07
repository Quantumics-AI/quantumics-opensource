import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteFolderDatasetComponent } from './delete-folder-dataset.component';

describe('DeleteFolderDatasetComponent', () => {
  let component: DeleteFolderDatasetComponent;
  let fixture: ComponentFixture<DeleteFolderDatasetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeleteFolderDatasetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteFolderDatasetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
