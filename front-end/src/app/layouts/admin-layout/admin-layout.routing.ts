import { Routes } from '@angular/router';

import { DashboardComponent } from '../../pages/dashboard/dashboard.component';
import { CategoryComponent } from 'app/pages/category/category.component';
import { AuthGuard } from 'app/pages/login/auth.guard';
import { SupplierComponent } from 'app/pages/supplier/supplier.component';
import { PayerComponent } from 'app/pages/payer/payer.component';
import { ExpenseComponent } from 'app/pages/expense/expense.component';

export const AdminLayoutRoutes: Routes = [
    { path: 'dashboard',      component: DashboardComponent, canActivate: [AuthGuard]  },
    { path: 'category',       component: CategoryComponent,  canActivate: [AuthGuard]  },
    { path: 'supplier',       component: SupplierComponent,  canActivate: [AuthGuard]  },
    { path: 'payer',          component: PayerComponent,     canActivate: [AuthGuard]  },
    { path: 'expense',        component: ExpenseComponent,   canActivate: [AuthGuard]  },
    { path: '**', redirectTo: 'dashboard' }
];
