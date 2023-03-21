import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'format'
})
export class FormatPipe implements PipeTransform {

  transform(value: string): unknown {

    if (!value) {
      return value;
    }

    const regex = /a_[0-9]*_/;
    return value?.replace(regex, '');
  }
}
