import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { PhotoDTO } from './Photos';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-photo-dialog',
  templateUrl: 'imagesdialog.html',
})
export class PhotoDialogComponent {
 apiUrl = environment.API_NO_BAR;
    
  constructor(@Inject(MAT_DIALOG_DATA) public data: PhotoDTO) { }
  
}
