import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WorkspaceDetailsComponent } from './workspace-details.component';

describe('WorkspaceDetailsComponent', () => {
  let component: WorkspaceDetailsComponent;
  let fixture: ComponentFixture<WorkspaceDetailsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkspaceDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkspaceDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
