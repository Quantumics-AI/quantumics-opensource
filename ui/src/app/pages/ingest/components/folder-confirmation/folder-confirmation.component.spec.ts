import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FolderConfirmationComponent } from './folder-confirmation.component';

describe('FolderConfirmationComponent', () => {
  let component: FolderConfirmationComponent;
  let fixture: ComponentFixture<FolderConfirmationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FolderConfirmationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FolderConfirmationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
