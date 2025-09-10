import { Routes } from '@angular/router';

import { DashboardComponent } from '../../pages/dashboard/dashboard.component';
import { CategoryComponent } from 'app/pages/category/category.component';
import { AuthGuard } from 'app/pages/login/auth.guard';
import { SupplierComponent } from 'app/pages/supplier/supplier.component';
import { PayerComponent } from 'app/pages/payer/payer.component';
import { ExpenseComponent } from 'app/pages/expense/expense.component';
import { ImagesComponent } from 'app/pages/images/images.component';
import { UserComponent } from 'app/pages/user/user.component';
import { ProfileComponent } from 'app/pages/profile/profile.component';

export const AdminLayoutRoutes: Routes = [
    { path: 'dashboard',      component: DashboardComponent, canActivate: [AuthGuard]  },
    { path: 'category',       component: CategoryComponent,  canActivate: [AuthGuard]  },
    { path: 'supplier',       component: SupplierComponent,  canActivate: [AuthGuard]  },
    { path: 'payer',          component: PayerComponent,     canActivate: [AuthGuard]  },
    { path: 'expense',        component: ExpenseComponent,   canActivate: [AuthGuard]  },
    { path: 'images',         component: ImagesComponent,    canActivate: [AuthGuard]  },
    { path: 'user',           component: UserComponent,      canActivate: [AuthGuard]  },
    { path: 'profile',        component: ProfileComponent,   canActivate: [AuthGuard]  },
    { path: '**', redirectTo: 'dashboard' },
];
