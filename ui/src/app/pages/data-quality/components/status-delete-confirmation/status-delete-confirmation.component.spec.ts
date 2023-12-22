import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusDeleteConfirmationComponent } from './status-delete-confirmation.component';

describe('StatusDeleteConfirmationComponent', () => {
  let component: StatusDeleteConfirmationComponent;
  let fixture: ComponentFixture<StatusDeleteConfirmationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StatusDeleteConfirmationComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StatusDeleteConfirmationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
