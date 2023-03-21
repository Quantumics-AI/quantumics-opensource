import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ListCleansedFilesComponent } from './list-cleansed-files.component';

describe('ListCleansedFilesComponent', () => {
  let component: ListCleansedFilesComponent;
  let fixture: ComponentFixture<ListCleansedFilesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ListCleansedFilesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListCleansedFilesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
