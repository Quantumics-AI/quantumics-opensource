import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditDataSourceComponent } from './edit-data-source.component';

describe('EditDataSourceComponent', () => {
  let component: EditDataSourceComponent;
  let fixture: ComponentFixture<EditDataSourceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditDataSourceComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditDataSourceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
