import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RenameWorkspaceComponent } from './rename-workspace.component';

describe('RenameWorkspaceComponent', () => {
  let component: RenameWorkspaceComponent;
  let fixture: ComponentFixture<RenameWorkspaceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RenameWorkspaceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RenameWorkspaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
