import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoveRowsComponent } from './remove-rows.component';

describe('RemoveRowsComponent', () => {
  let component: RemoveRowsComponent;
  let fixture: ComponentFixture<RemoveRowsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RemoveRowsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RemoveRowsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
