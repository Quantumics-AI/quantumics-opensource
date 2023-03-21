import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { GlossaryListComponent } from './glossary-list.component';

describe('GlossaryListComponent', () => {
  let component: GlossaryListComponent;
  let fixture: ComponentFixture<GlossaryListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ GlossaryListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GlossaryListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
