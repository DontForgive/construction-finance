import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DashboardComponent } from '../../pages/dashboard/dashboard.component';
import { UserComponent } from '../../pages/user/user.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { CategoryComponent } from 'app/pages/category/category.component';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatDialogModule } from '@angular/material/dialog';
import { CategoryAddDialogComponent } from 'app/pages/category/category-add-dialog.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ToastrModule } from 'ngx-toastr';
import { AdminLayoutRoutes } from './admin-layout.routing';
import { SupplierComponent } from 'app/pages/supplier/supplier.component';
import { SupplierAddDialogComponent } from 'app/pages/supplier/supplier-add-dialog.component';
import { PayerAddDialogComponent } from 'app/pages/payer/payer-add-dialog.component';
import { PayerComponent } from 'app/pages/payer/payer.component';
import { ExpenseComponent } from 'app/pages/expense/expense.component';
import { ExpenseAddDialogComponent } from 'app/pages/expense/expense-add-dialog.component';
import { MatSelectModule } from '@angular/material/select';
import { MAT_DATE_LOCALE, MatOptionModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';
import { ImagesComponent } from 'app/pages/images/images.component';
import { PhotoDialogComponent } from 'app/pages/images/photodialog.component';



@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(AdminLayoutRoutes),
    NgbModule,
    MatPaginatorModule,
    MatDialogModule,
    MatFormFieldModule, // <-- Adicione aqui!
    MatInputModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    ToastrModule.forRoot({
      positionClass: 'toast-top-right', // canto superior direito
      timeOut: 1500,
      closeButton: true,
      progressBar: true,
      preventDuplicates: true,
    }),
    FormsModule,
    MatSelectModule,
    MatOptionModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCardModule,
    

  ],
  declarations: [
    DashboardComponent,
    UserComponent,

    /*  MY PAGES E COMPONENTS  */
    CategoryComponent,
    SupplierComponent,
    CategoryAddDialogComponent,
    SupplierAddDialogComponent,
    PayerComponent,
    PayerAddDialogComponent,
    ExpenseComponent,
    ExpenseAddDialogComponent,
    ImagesComponent,
    PhotoDialogComponent
    

  ],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' },
   
  ],
})

export class AdminLayoutModule { }
