import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataQualityShowDataComponent } from './data-quality-show-data.component';

describe('DataQualityShowDataComponent', () => {
  let component: DataQualityShowDataComponent;
  let fixture: ComponentFixture<DataQualityShowDataComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DataQualityShowDataComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DataQualityShowDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
