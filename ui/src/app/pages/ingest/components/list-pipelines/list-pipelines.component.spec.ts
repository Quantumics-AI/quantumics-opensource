import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ListPipelinesComponent } from './list-pipelines.component';

describe('ListPipelinesComponent', () => {
  let component: ListPipelinesComponent;
  let fixture: ComponentFixture<ListPipelinesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ListPipelinesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListPipelinesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
