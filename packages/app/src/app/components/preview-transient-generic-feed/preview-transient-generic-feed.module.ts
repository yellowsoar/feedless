import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PreviewTransientGenericFeedComponent } from './preview-transient-generic-feed.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { ImportTransientGenericFeedModule } from '../import-transient-generic-feed/import-transient-generic-feed.module';



@NgModule({
  declarations: [PreviewTransientGenericFeedComponent],
  exports: [PreviewTransientGenericFeedComponent],
  imports: [
    CommonModule,
    IonicModule,
    ReactiveFormsModule,
    ImportTransientGenericFeedModule
  ]
})
export class PreviewTransientGenericFeedModule { }
