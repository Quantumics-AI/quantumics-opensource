import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateAwsComponent } from './create-aws.component';

describe('CreateAwsComponent', () => {
  let component: CreateAwsComponent;
  let fixture: ComponentFixture<CreateAwsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateAwsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateAwsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
