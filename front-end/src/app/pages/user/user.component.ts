import { Component, OnInit } from '@angular/core';
import { UserService } from './user.service';
import { MatDialog } from '@angular/material/dialog';
import { ToastService } from 'app/utils/toastr';
import { User } from './user';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 5;
  pageSizes: number[] = [5, 10, 20, 50];

  constructor(private service: UserService, private dialog: MatDialog,
    private toast: ToastService
  ) { }

  public list_users: User[] = []; 
  FilterUsername: string = '';
  filterEmail: string = '';


  ngOnInit() {
    this.listUsers(0);
  }

  listUsers(page:number = 0){

    this.currentPage = page;

    this.service.getUsers(
      this.currentPage,
      this.pageSize,
      'id',
      'DESC',
      this.FilterUsername,
      this.filterEmail
    ).subscribe((res) =>{
      this.list_users = res.data.content;
      this.totalElements = res.data.totalElements;
      this.totalPages = res.data.totalPages;
    },
    (error) => {
      this.toast.error('Erro ao buscar os usuários \n' + error.error[0], "Error");
      console.log("error: ", error.message);
    }
  )}

  clearFilters() {
    this.FilterUsername = '';
    this.filterEmail = '';
    this.listUsers(0);
  }
    onPageChange(event: any) {
    this.pageSize = event.pageSize;
    this.listUsers(event.pageIndex);
  }


}
