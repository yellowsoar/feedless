import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { NotificationsPageRoutingModule } from './notifications-routing.module';

import { NotificationsPage } from './notifications.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    NotificationsPageRoutingModule,
    PageHeaderModule,
  ],
  declarations: [NotificationsPage],
})
export class NotificationsPageModule {}
