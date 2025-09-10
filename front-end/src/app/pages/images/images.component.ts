import { Component, OnInit } from "@angular/core";
import { PhotoDTO } from "./Photos";
import { ImagesService } from "./images.service";
import { MatDialog } from "@angular/material/dialog";
import { environment } from "environments/environment";
import { HttpEventType } from "@angular/common/http";
import Swal from "sweetalert2";

@Component({
  selector: "app-images",
  templateUrl: "./images.component.html",
  styleUrls: ["./images.component.scss"],
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
    "Janeiro",
    "Fevereiro",
    "Março",
    "Abril",
    "Maio",
    "Junho",
    "Julho",
    "Agosto",
    "Setembro",
    "Outubro",
    "Novembro",
    "Dezembro",
  ];

  constructor(
    private imagesService: ImagesService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadYears();
  }

  loadYears(): void {
    this.imagesService.listYears().subscribe({
      next: (res) => (this.years = res.data),
      error: (err) => console.error(err),
    });
  }

  openYear(year: number): void {
    this.selectedYear = year;
    this.imagesService.listMonths(year).subscribe({
      next: (res) => (
        (this.months = res.data), console.log("data: ", res.data)
      ),
      error: (err) => console.error(err),
    });
  }

  openMonth(month: number): void {
    const videoExtensions = [".mp4", ".mov", ".avi", ".mkv", ".webm"];

    if (!this.selectedYear) return;
    this.selectedMonth = month;
    this.imagesService.listPhotos(this.selectedYear, month).subscribe({
      next: (res) => {
        this.photos = res.data.map((item) => {
          const lower = item.url.toLowerCase();
          const isVideo = videoExtensions.some((ext) => lower.endsWith(ext));
          return { ...item, type: isVideo ? "video" : "photo" };
        });
      },
      error: (err) => console.error(err),
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
          console.log("Upload concluído:", event.body);

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
        console.error("Erro ao enviar foto", err);
      },
    });
  }

  openPhoto(index: number): void {
    this.activeIndex = index;
    this.displayFullscreen = true;
  }

  onDeleteActivePhoto(photo){
    console.log("Photo: ", photo);
  }

  onDeletePhoto(index: number): void {
    const photo = this.photos[index];
    console.log("Nome da foto:" , photo?.filename);
    const name = photo?.filename || photo?.url || "esta mídia";
    const year = this.selectedYear || 0;
    const month = this.selectedMonth || 0;

    Swal.fire({
      title: "Tem certeza?",
      text: `A mídia "${name}" será excluída!`,
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#d33",
      cancelButtonColor: "#6c757d",
      confirmButtonText: "Sim, excluir",
      cancelButtonText: "Cancelar",
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.imagesService.deletePhoto(year, month, name).subscribe((result) => {
        console.log("Status: ", result.status);
        if (result.status != 200) {
          Swal.fire({
            icon: "error",
            title: "Erro ao excluir!",
            text: result.message,
            showConfirmButton: true,
          });
        } else {
          Swal.fire({
            icon: "success",
            title: "Excluída!",
            text: "A mídia foi removida da lista.",
            showConfirmButton: false,
            timer: 1000,
          });
        }
        console.log("result: ", result);
      });

      this.photos.splice(index, 1);

      if (this.displayFullscreen) {
        if (this.photos.length === 0) {
          this.displayFullscreen = false;
        } else if (this.activeIndex >= this.photos.length) {
          this.activeIndex = Math.max(0, this.photos.length - 1);
        }
      }
    });
  }
}
