import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectRestoredComponent } from './project-restored.component';

describe('ProjectRestoredComponent', () => {
  let component: ProjectRestoredComponent;
  let fixture: ComponentFixture<ProjectRestoredComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectRestoredComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectRestoredComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
