import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProjectsStatsComponent } from './projects-stats.component';

describe('ProjectsStatsComponent', () => {
  let component: ProjectsStatsComponent;
  let fixture: ComponentFixture<ProjectsStatsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectsStatsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectsStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
