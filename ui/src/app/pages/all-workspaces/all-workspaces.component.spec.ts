import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AllWorkspacesComponent } from './all-workspaces.component';

describe('AllWorkspacesComponent', () => {
  let component: AllWorkspacesComponent;
  let fixture: ComponentFixture<AllWorkspacesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AllWorkspacesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AllWorkspacesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
