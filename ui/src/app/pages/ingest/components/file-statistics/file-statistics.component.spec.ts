import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FileStatisticsComponent } from './file-statistics.component';

describe('FileStatisticsComponent', () => {
  let component: FileStatisticsComponent;
  let fixture: ComponentFixture<FileStatisticsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FileStatisticsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FileStatisticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
