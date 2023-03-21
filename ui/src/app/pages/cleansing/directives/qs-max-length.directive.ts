import { Directive, HostListener } from '@angular/core';

@Directive({
  selector: '[appQsMaxLength]'
})
export class QsMaxLengthDirective {
  @HostListener('keypress', ['$event'])

  public onkeypress(ev: any): boolean {
    // 69 for E, 101 for e
    if (ev.target.value.length === 9 || ev.charCode === 69 || ev.charCode === 101) {
      return false;
    }
  }
}

@Directive({
  selector: '[appPositiveNumberOnly]'
})
export class PositiveNumberOnlyDirective {
  @HostListener('keypress', ['$event'])

  public onkeypress(ev: any): boolean {
    // 69 for E, 101 for e
    if (ev.target.value.length === 9
      || ev.charCode === 69
      || ev.charCode === 101
      || ev.charCode === 43
      || ev.charCode === 45
      || ev.charCode === 46) {
      return false;
    }
  }
}
