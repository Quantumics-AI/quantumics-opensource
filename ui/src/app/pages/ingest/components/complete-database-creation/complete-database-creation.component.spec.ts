import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CompleteDatabaseCreationComponent } from './complete-database-creation.component';

describe('CompleteDatabaseCreationComponent', () => {
  let component: CompleteDatabaseCreationComponent;
  let fixture: ComponentFixture<CompleteDatabaseCreationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CompleteDatabaseCreationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CompleteDatabaseCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
