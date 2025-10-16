/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { WorkdayComponent } from './workday.component';

describe('WorkdayComponent', () => {
  let component: WorkdayComponent;
  let fixture: ComponentFixture<WorkdayComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkdayComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkdayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
