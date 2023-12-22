import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AwsConfirmationComponent } from './aws-confirmation.component';

describe('AwsConfirmationComponent', () => {
  let component: AwsConfirmationComponent;
  let fixture: ComponentFixture<AwsConfirmationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AwsConfirmationComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AwsConfirmationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
