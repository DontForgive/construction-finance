import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AuthLayoutComponent } from './auth-layout.component';
import { AuthLayoutRoutes } from './auth-layout.routing';
import { LoginComponent } from 'app/pages/login/login.component';
import { ResetPasswordComponent } from 'app/pages/reset-password/reset-password.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(AuthLayoutRoutes),
  ],
  declarations: [
    AuthLayoutComponent, // precisa estar aqui
    LoginComponent,       // precisa estar aqui
    ResetPasswordComponent
  ]
})
export class AuthLayoutModule {}
