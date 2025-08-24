import { Component, OnInit } from '@angular/core';


export interface RouteInfo {
    path: string;
    title: string;
    icon: string;
    class: string;
}

export const ROUTES: RouteInfo[] = [
    { path: '/dashboard',     title: 'Dashboard',    icon:'nc-chart-bar-32',   class: '' },
    { path: '/expense',       title: 'Lançamentos',  icon:'nc-money-coins',    class: '' },
    { path: '/images', title: 'Galeria', icon: 'nc-image', class: '' },
    { path: '/category',      title: 'Categorias',   icon:'nc-bullet-list-67', class: '' },
    { path: '/supplier',      title: 'Fornecedores', icon:'nc-briefcase-24',   class: '' },
    { path: '/payer',         title: 'Pagadores',    icon:'nc-credit-card',    class: '' },
    { path: '/user',          title: 'Usuários',     icon:'nc-circle-10',      class: '' },    
];

@Component({
    moduleId: module.id,
    selector: 'sidebar-cmp',
    templateUrl: 'sidebar.component.html',
})

export class SidebarComponent implements OnInit {
    public menuItems: any[];
    ngOnInit() {
        this.menuItems = ROUTES.filter(menuItem => menuItem);
    }
}
