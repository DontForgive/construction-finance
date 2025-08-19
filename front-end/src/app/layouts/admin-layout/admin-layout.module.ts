import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { DashboardComponent } from '../../pages/dashboard/dashboard.component';
import { UserComponent } from '../../pages/user/user.component';
import { TableComponent } from '../../pages/table/table.component';
import { TypographyComponent } from '../../pages/typography/typography.component';
import { IconsComponent } from '../../pages/icons/icons.component';
import { MapsComponent } from '../../pages/maps/maps.component';
import { NotificationsComponent } from '../../pages/notifications/notifications.component';
import { UpgradeComponent } from '../../pages/upgrade/upgrade.component';

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



@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(AdminLayoutRoutes ),
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
    FormsModule          

  ],
  declarations: [
    DashboardComponent,
    UserComponent,
    TableComponent,
    UpgradeComponent,
    TypographyComponent,
    IconsComponent,
    MapsComponent,
    NotificationsComponent,

    /*  MY PAGES E COMPONENTS  */
    CategoryComponent,
    SupplierComponent,
    CategoryAddDialogComponent,
    SupplierAddDialogComponent
  ]
})

export class AdminLayoutModule { }
