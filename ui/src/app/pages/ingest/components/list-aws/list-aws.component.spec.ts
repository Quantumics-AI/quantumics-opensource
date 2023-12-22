import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListAwsComponent } from './list-aws.component';

describe('ListAwsComponent', () => {
  let component: ListAwsComponent;
  let fixture: ComponentFixture<ListAwsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListAwsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListAwsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
