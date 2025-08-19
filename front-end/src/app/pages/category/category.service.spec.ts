import { CategoryComponent } from './category.component';
import { CategoryService } from './category.service';
import { MatPaginatorModule } from '@angular/material/paginator';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { TestBed, async } from '@angular/core/testing';

describe('CategoryComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CategoryComponent ],
      imports: [ MatPaginatorModule, HttpClientTestingModule ],
      providers: [ CategoryService ]
    })
    .compileComponents();
  }));

  // ... seus testes aqui ...
});