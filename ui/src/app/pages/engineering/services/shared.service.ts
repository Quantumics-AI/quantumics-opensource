import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  private engFlow = new BehaviorSubject<any>('');
  flow = this.engFlow.asObservable();

  constructor() { }

  createFlow(flow: any) {
    this.engFlow.next(flow)
  }
}
