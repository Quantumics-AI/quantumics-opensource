import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { IngestComponent } from './ingest.component';

describe('FoldersComponent', () => {
  let component: IngestComponent;
  let fixture: ComponentFixture<IngestComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [IngestComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
