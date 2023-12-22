import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataQualityCreateComponent } from './data-quality-create.component';

describe('DataQualityCreateComponent', () => {
  let component: DataQualityCreateComponent;
  let fixture: ComponentFixture<DataQualityCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DataQualityCreateComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DataQualityCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
