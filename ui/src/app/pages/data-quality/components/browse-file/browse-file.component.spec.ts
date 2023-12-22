import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrowseFileComponent } from './browse-file.component';

describe('BrowseFileComponent', () => {
  let component: BrowseFileComponent;
  let fixture: ComponentFixture<BrowseFileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BrowseFileComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BrowseFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
