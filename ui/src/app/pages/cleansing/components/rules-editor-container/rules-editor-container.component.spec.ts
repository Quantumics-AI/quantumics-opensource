import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RulesEditorContainerComponent } from './rules-editor-container.component';

describe('RulesEditorContainerComponent', () => {
  let component: RulesEditorContainerComponent;
  let fixture: ComponentFixture<RulesEditorContainerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RulesEditorContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RulesEditorContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
