import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataQualityListComponent } from './data-quality-list.component';

describe('DataQualityListComponent', () => {
  let component: DataQualityListComponent;
  let fixture: ComponentFixture<DataQualityListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DataQualityListComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DataQualityListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
