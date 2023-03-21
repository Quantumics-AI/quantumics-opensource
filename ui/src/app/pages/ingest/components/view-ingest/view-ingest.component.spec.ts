import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ViewIngestComponent } from './view-ingest.component';

describe('ViewIngestComponent', () => {
  let component: ViewIngestComponent;
  let fixture: ComponentFixture<ViewIngestComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewIngestComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewIngestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
