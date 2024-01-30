import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectLimitComponent } from './project-limit.component';

describe('ProjectLimitComponent', () => {
  let component: ProjectLimitComponent;
  let fixture: ComponentFixture<ProjectLimitComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectLimitComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectLimitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
