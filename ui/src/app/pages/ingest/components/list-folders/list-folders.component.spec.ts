import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ListFoldersComponent } from './list-folders.component';

describe('ListFoldersComponent', () => {
  let component: ListFoldersComponent;
  let fixture: ComponentFixture<ListFoldersComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ListFoldersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListFoldersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
