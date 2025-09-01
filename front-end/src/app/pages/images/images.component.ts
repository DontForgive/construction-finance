
import { Component, OnInit } from '@angular/core';
import { PhotoDTO } from './Photos';
import { ImagesService } from './images.service';
import { MatDialog } from '@angular/material/dialog';
import { environment } from 'environments/environment';
import { HttpEventType } from '@angular/common/http';

@Component({
  selector: 'app-images',
  templateUrl: './images.component.html',
  styleUrls: ['./images.component.scss']
})
export class ImagesComponent implements OnInit {

  apiUrl = environment.API_NO_BAR;
  years: number[] = [];
  months: number[] = [];
  photos: PhotoDTO[] = [];

  selectedYear: number | null = null;
  selectedMonth: number | null = null;

  displayFullscreen: boolean = false;
  activeIndex: number = 0;

  // progresso do upload
  uploading = false;
  progress = 0;

  monthNames = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
  ];

  constructor(private imagesService: ImagesService, private dialog: MatDialog) { }

  ngOnInit(): void {
    this.loadYears();
  }

  loadYears(): void {
    this.imagesService.listYears().subscribe({
      next: (res) => this.years = res.data,
      error: (err) => console.error(err)
    });
  }

  openYear(year: number): void {
    this.selectedYear = year;
    this.imagesService.listMonths(year).subscribe({
      next: (res) => (this.months = res.data, console.log("data: ", res.data)),
      error: (err) => console.error(err)
    });
  }

  openMonth(month: number): void {
    const videoExtensions = ['.mp4', '.mov', '.avi', '.mkv', '.webm'];

    if (!this.selectedYear) return;
    this.selectedMonth = month;
    this.imagesService.listPhotos(this.selectedYear, month).subscribe({
      next: (res) => {
        this.photos = res.data.map(item => {
          const lower = item.url.toLowerCase();
          const isVideo = videoExtensions.some(ext => lower.endsWith(ext));
          return { ...item, type: isVideo ? 'video' : 'photo' };
        });
      },
      error: (err) => console.error(err)
    });
  }

  backToYears(): void {
    this.selectedYear = null;
    this.selectedMonth = null;
    this.months = [];
    this.photos = [];
  }

  backToMonths(): void {
    this.selectedMonth = null;
    this.photos = [];
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    this.uploading = true;
    this.progress = 0;

    this.imagesService.uploadWithProgress(file).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.progress = Math.round((100 * event.loaded) / event.total);
        } else if (event.type === HttpEventType.Response) {
          this.uploading = false;
          console.log('Upload concluído:', event.body);

          // recarrega as fotos de acordo com a seleção
          if (this.selectedYear && this.selectedMonth) {
            this.openMonth(this.selectedMonth);
          } else if (this.selectedYear) {
            this.openYear(this.selectedYear);
          } else {
            this.loadYears();
          }
        }
      },
      error: (err) => {
        this.uploading = false;
        console.error('Erro ao enviar foto', err);
      }
    });
  }

  openPhoto(index: number): void {
    this.activeIndex = index;
    this.displayFullscreen = true;
  }

}


// import { Component, OnInit } from '@angular/core';
// import { PhotoDTO } from './Photos';
// import { ImagesService } from './images.service';
// import { MatDialog } from '@angular/material/dialog';
// import { PhotoDialogComponent } from './photodialog.component';
// import { environment } from 'environments/environment';

// @Component({
//   selector: 'app-images',
//   templateUrl: './images.component.html',
//   styleUrls: ['./images.component.scss']
// })
// export class ImagesComponent implements OnInit {


//   apiUrl = environment.API_NO_BAR;
//   years: number[] = [];
//   months: number[] = [];
//   photos: PhotoDTO[] = [];

//   selectedYear: number | null = null;
//   selectedMonth: number | null = null;

//   displayFullscreen: boolean = false;
//   activeIndex: number = 0;

//   monthNames = [
//     'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
//     'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
//   ];

//   constructor(private imagesService: ImagesService, private dialog: MatDialog) { }

//   ngOnInit(): void {
//     this.loadYears();
    
//   }

//   loadYears(): void {
//     this.imagesService.listYears().subscribe({
//       next: (res) => this.years = res.data, // ⚠️ se sua API também retorna wrapper, use res.data
//       error: (err) => console.error(err)
//     });
//   }

//   openYear(year: number): void {
//     this.selectedYear = year;
//     this.imagesService.listMonths(year).subscribe({
//       next: (res) => (this.months = res.data, console.log("data: ", res.data)), // ⚠️ se sua API também retorna wrapper, use res.data
//       error: (err) => console.error(err)
//     });
//   }

//   openMonth(month: number): void {

//     const videoExtensions = ['.mp4', '.mov', '.avi', '.mkv', '.webm'];

//     if (!this.selectedYear) return;
//     this.selectedMonth = month;
//     this.imagesService.listPhotos(this.selectedYear, month).subscribe({
//       next: (res) => {
//         this.photos = res.data.map(item => {
//           console.log("item.url: ", item.url);
//           const lower = item.url.toLowerCase();
//           const isVideo = videoExtensions.some(ext => lower.endsWith(ext));
//           return { ...item, type: isVideo ? 'video' : 'photo' };
//         });
//       },
//       error: (err) => console.error(err)
//     });
//   }


//   backToYears(): void {
//     this.selectedYear = null;
//     this.selectedMonth = null;
//     this.months = [];
//     this.photos = [];
//   }

//   backToMonths(): void {
//     this.selectedMonth = null;
//     this.photos = [];
//   }

//   onFileSelected(event: Event): void {
//     const input = event.target as HTMLInputElement;
//     if (!input.files || input.files.length === 0) return;

//     const file = input.files[0];
//     this.imagesService.upload(file).subscribe({
//       next: () => {
//         if (this.selectedYear && this.selectedMonth) {
//           this.openMonth(this.selectedMonth);
//         } else if (this.selectedYear) {
//           this.openYear(this.selectedYear);
//         } else {
//           this.loadYears();
//         }
//       },
//       error: (err) => console.error('Erro ao enviar foto', err)
//     });
//   }

//   openPhoto(index: number): void {
//     this.activeIndex = index;
//     this.displayFullscreen = true;
//   }


// }