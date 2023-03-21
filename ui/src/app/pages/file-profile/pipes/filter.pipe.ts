import { Pipe, PipeTransform } from '@angular/core';
import * as lodash from 'lodash';

@Pipe({ name: 'filterBy' })
export class FilterPipe implements PipeTransform {
  transform(value, keys: string, term: string): any[] {
    if (!term) {
      return value;
    }

    return (value || [])
      .filter(item =>
        keys.split(',').some(key => {
          try {
            const val = lodash.get(item, key.trim(), undefined);
            return val !== undefined && new RegExp(`${term}`, 'gi').test(val);
          } catch (error) {
            const val = item[key?.trim()].toString();
            return val?.toLowerCase()?.includes(term?.toLowerCase());
          }
        })
      );
  }
}


@Pipe({ name: 'filterColumn' })
export class FilterColumnPipe implements PipeTransform {
  transform(items: any[], searchText: string): any[] {
    if (!items) { return []; }
    if (!searchText) { return items; }

    return (items).filter(item => {
      return String(item.column).toLowerCase().includes(searchText.toLowerCase());
    });
  }
}
