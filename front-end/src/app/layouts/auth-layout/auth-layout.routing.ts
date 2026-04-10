import { Routes } from '@angular/router';
import { LoginComponent } from 'app/pages/login/login.component';
import { ResetPasswordComponent } from 'app/pages/reset-password/reset-password.component';

export const AuthLayoutRoutes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'reset-password', component: ResetPasswordComponent }
];
