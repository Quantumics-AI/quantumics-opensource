import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoveRowsByColumnValuesComponent } from './remove-rows-by-column-values.component';

describe('RemoveRowsByColumnValuesComponent', () => {
  let component: RemoveRowsByColumnValuesComponent;
  let fixture: ComponentFixture<RemoveRowsByColumnValuesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RemoveRowsByColumnValuesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RemoveRowsByColumnValuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
