import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConfigureFolderComponent } from './configure-folder.component';

describe('ConfigureFolderComponent', () => {
  let component: ConfigureFolderComponent;
  let fixture: ComponentFixture<ConfigureFolderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigureFolderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigureFolderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
