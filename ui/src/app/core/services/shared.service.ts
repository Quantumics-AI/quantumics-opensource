import { Injectable, EventEmitter } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SharedService {
  pushProjectId = new EventEmitter();
  notificationCount = new EventEmitter();
  workSpaceUpdate = new EventEmitter();
  deActiveWorkSpace = new EventEmitter();
}
