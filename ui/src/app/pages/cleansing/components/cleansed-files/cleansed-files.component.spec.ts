import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CleansedFilesComponent } from './cleansed-files.component';

describe('CleansedFilesComponent', () => {
  let component: CleansedFilesComponent;
  let fixture: ComponentFixture<CleansedFilesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CleansedFilesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CleansedFilesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
