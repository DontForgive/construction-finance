import { Component, OnInit, Renderer2, ViewChild, ElementRef } from '@angular/core';
import { ROUTES } from '../../sidebar/sidebar.component';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { AuthService } from 'app/pages/login/auth.service';
import { ProfileService } from 'app/pages/profile/profile.service';
import { User } from 'app/pages/user/user';
import { environment } from '../../../environments/environment';

@Component({
  moduleId: module.id,
  selector: 'navbar-cmp',
  templateUrl: 'navbar.component.html'
})
export class NavbarComponent implements OnInit {
  private listTitles: any[];
  location: Location;
  private nativeElement: Node;
  private toggleButton;
  private sidebarVisible: boolean;

  public isCollapsed = true;
  @ViewChild('navbar-cmp', { static: false }) button;

  private profileUser: User | null = null;

  constructor(
    location: Location,
    private renderer: Renderer2,
    private element: ElementRef,
    private router: Router,
    private authService: AuthService,
    private profileService: ProfileService
  ) {
    this.location = location;
    this.nativeElement = element.nativeElement;
    this.sidebarVisible = false;
  }

  ngOnInit(): void {
    this.listTitles = ROUTES.filter(listTitle => listTitle);

    const navbar: HTMLElement = this.element.nativeElement;
    this.toggleButton = navbar.getElementsByClassName('navbar-toggle')[0];

    this.router.events.subscribe(() => {
      this.sidebarClose();
    });

    this.profileService.profile$.subscribe((user) => {
      this.profileUser = user;
    });

    this.profileService.refreshProfile();
  }

  getTitle(): string {
    let titlee = this.location.prepareExternalUrl(this.location.path());
    if (titlee.charAt(0) === '#') {
      titlee = titlee.slice(1);
    }
    for (let item = 0; item < this.listTitles.length; item++) {
      if (this.listTitles[item].path === titlee) {
        return this.listTitles[item].title;
      }
    }
    return 'Dashboard';
  }

  sidebarToggle(): void {
    if (this.sidebarVisible === false) {
      this.sidebarOpen();
    } else {
      this.sidebarClose();
    }
  }

  sidebarOpen(): void {
    const toggleButton = this.toggleButton;
    const html = document.getElementsByTagName('html')[0];
    const mainPanel = document.getElementsByClassName('main-panel')[0] as HTMLElement;

    setTimeout(function () {
      toggleButton.classList.add('toggled');
    }, 500);

    html.classList.add('nav-open');
    if (window.innerWidth < 991) {
      mainPanel.style.position = 'fixed';
    }
    this.sidebarVisible = true;
  }

  sidebarClose(): void {
    const html = document.getElementsByTagName('html')[0];
    const mainPanel = document.getElementsByClassName('main-panel')[0] as HTMLElement;

    if (window.innerWidth < 991) {
      setTimeout(function () {
        mainPanel.style.position = '';
      }, 500);
    }

    this.toggleButton.classList.remove('toggled');
    this.sidebarVisible = false;
    html.classList.remove('nav-open');
  }

  collapse(): void {
    this.isCollapsed = !this.isCollapsed;
    const navbar = document.getElementsByTagName('nav')[0];

    if (!this.isCollapsed) {
      navbar.classList.remove('navbar-transparent');
      navbar.classList.add('bg-white');
    } else {
      navbar.classList.add('navbar-transparent');
      navbar.classList.remove('bg-white');
    }
  }

  get username(): string {
    if (this.profileUser?.username) {
      return this.profileUser.username;
    }

    const tokenUser = this.authService.getUser();
    return tokenUser?.sub || tokenUser?.username || 'Usuário';
  }

  get profilePicture(): string {
    const baseUrl = environment.API_NO_BAR;

    const raw =
      this.profileUser?.profilePictureUrl ||
      this.authService.getUser()?.picture ||
      '';

    if (!raw) {
      return 'assets/img/faces/face-3.jpg';
    }

    if (typeof raw === 'string' && (raw.startsWith('http://') || raw.startsWith('https://'))) {
      return raw;
    }

    return `${baseUrl}${raw}`;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
