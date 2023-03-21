import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CompleteFolderCreationComponent } from './complete-folder-creation.component';

describe('CompleteFolderCreationComponent', () => {
  let component: CompleteFolderCreationComponent;
  let fixture: ComponentFixture<CompleteFolderCreationComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CompleteFolderCreationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CompleteFolderCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
