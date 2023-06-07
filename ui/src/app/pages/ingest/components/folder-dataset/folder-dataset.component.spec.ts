import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FolderDatasetComponent } from './folder-dataset.component';

describe('FolderDatasetComponent', () => {
  let component: FolderDatasetComponent;
  let fixture: ComponentFixture<FolderDatasetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FolderDatasetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FolderDatasetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
