import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EngineerContainerComponent } from './engineer-container.component';

describe('EngineerContainerComponent', () => {
  let component: EngineerContainerComponent;
  let fixture: ComponentFixture<EngineerContainerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EngineerContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EngineerContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
