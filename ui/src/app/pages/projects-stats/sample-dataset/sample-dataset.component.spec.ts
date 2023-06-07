import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SampleDatasetComponent } from './sample-dataset.component';

describe('SampleDatasetComponent', () => {
  let component: SampleDatasetComponent;
  let fixture: ComponentFixture<SampleDatasetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SampleDatasetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SampleDatasetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
