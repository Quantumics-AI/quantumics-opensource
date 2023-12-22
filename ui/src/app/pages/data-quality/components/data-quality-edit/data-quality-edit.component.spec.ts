import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataQualityEditComponent } from './data-quality-edit.component';

describe('DataQualityEditComponent', () => {
  let component: DataQualityEditComponent;
  let fixture: ComponentFixture<DataQualityEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DataQualityEditComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DataQualityEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
