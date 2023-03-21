import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProjectGridViewComponent } from './project-grid-view.component';

describe('ProjectGridViewComponent', () => {
  let component: ProjectGridViewComponent;
  let fixture: ComponentFixture<ProjectGridViewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectGridViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectGridViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
