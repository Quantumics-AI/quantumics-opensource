import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoveDuplicatesComponent } from './remove-duplicates.component';

describe('RemoveDuplicatesComponent', () => {
  let component: RemoveDuplicatesComponent;
  let fixture: ComponentFixture<RemoveDuplicatesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RemoveDuplicatesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RemoveDuplicatesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
