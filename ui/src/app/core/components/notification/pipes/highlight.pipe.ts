import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'highlight'
})
export class HighlightPipe implements PipeTransform {

  transform(value: string): unknown {
    const regex = /(["'])(.*?)\1/g;
    return value?.replace(regex, '<span class="noti-success-text">$2</span>');
  }
}
