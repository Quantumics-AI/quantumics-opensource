import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardFolderComponent } from './wizard-folder.component';

describe('WizardFolderComponent', () => {
  let component: WizardFolderComponent;
  let fixture: ComponentFixture<WizardFolderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ WizardFolderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WizardFolderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
