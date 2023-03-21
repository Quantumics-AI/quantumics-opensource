import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DataService {

  private source = new BehaviorSubject<number>(0);
  public progress = this.source.asObservable()

  constructor() { }

  updateProgress(progress: number) {
    this.source.next(progress);
  }
}
