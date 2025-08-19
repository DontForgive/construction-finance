import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  constructor(private toastr: ToastrService) {}

  success(message: string, title: string = '', timeOut: number = 2000) {
    this.toastr.success(
      `<span data-notify="icon" class="nc-icon nc-check-2"></span>` +
      `<span data-notify="message">${message}</span>`,
      title,
      {
        timeOut: timeOut,
        closeButton: true,
        enableHtml: true,
        toastClass: "alert alert-success alert-with-icon",
        positionClass: "toast-top-right"
      }
    );
  }

  error(message: string, title: string = '', timeOut: number = 2000) {
    this.toastr.error(
      `<span data-notify="icon" class="nc-icon nc-simple-remove"></span>` +
      `<span data-notify="message">${message}</span>`,
      title,
      {
        timeOut: timeOut,
        closeButton: true,
        enableHtml: true,
        toastClass: "alert alert-danger alert-with-icon",
        positionClass: "toast-top-right"
      }
    );
  }

  warning(message: string, title: string = '', timeOut: number = 2000) {
    this.toastr.warning(
      `<span data-notify="icon" class="nc-icon nc-bell-55"></span>` +
      `<span data-notify="message">${message}</span>`,
      title,
      {
        timeOut: timeOut,
        closeButton: true,
        enableHtml: true,
        toastClass: "alert alert-warning alert-with-icon",
        positionClass: "toast-top-right"
      }
    );
  }

  info(message: string, title: string = '', timeOut: number = 2000) {
    this.toastr.info(
      `<span data-notify="icon" class="nc-icon nc-alert-circle-i"></span>` +
      `<span data-notify="message">${message}</span>`,
      title,
      {
        timeOut: timeOut,
        closeButton: true,
        enableHtml: true,
        toastClass: "alert alert-info alert-with-icon",
        positionClass: "toast-top-right"
      }
    );
  }
}
